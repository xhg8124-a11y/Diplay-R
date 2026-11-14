package com.shilapi.xcertplay.media

import android.Manifest
import android.media.AudioRecord
import android.media.MediaRecorder
import com.shilapi.xcertplay.airplay.MicrophoneConfig
import java.net.InetAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAudioRecord

/**
 * A firmware that delivers zeros on one capture source gets the next one tried: measured on
 * the Feifan F7, a whole call ran with reads, encodes and sends all healthy while every
 * sample was zero (peakDbfs=silent) under a system service that holds the microphone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [25, 29], manifest = Config.NONE)
class MicrophoneSilentSourceRotationTest {
    private val diagnostics = CopyOnWriteArrayList<String>()
    private val reads = AtomicInteger()
    private val created = CopyOnWriteArrayList<AudioRecord>()
    private var uplink: MicrophoneUplink? = null

    @Before fun setUp() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.RECORD_AUDIO)
    }

    @After fun tearDown() {
        uplink?.close()
        ShadowAudioRecord.clearSource()
    }

    @Test fun theRotationWalksTheSourcesAfterTheCurrentOne() {
        assertEquals(
            listOf(MediaRecorder.AudioSource.MIC, MediaRecorder.AudioSource.VOICE_RECOGNITION),
            silentSourceRotation(MediaRecorder.AudioSource.VOICE_COMMUNICATION),
        )
        assertEquals(
            listOf(MediaRecorder.AudioSource.VOICE_RECOGNITION, MediaRecorder.AudioSource.VOICE_COMMUNICATION),
            silentSourceRotation(MediaRecorder.AudioSource.MIC),
        )
        assertEquals(3, silentSourceRotation(Int.MIN_VALUE).size)
    }

    @Test fun aZeroBufferCarriesNoSound() {
        assertFalse(bufferCarriesSound(ByteArray(64), 64))
        assertTrue(bufferCarriesSound(byteArrayOf(0, 1) + ByteArray(62), 64))
        // A non-zero high byte alone is a real sample too.
        assertTrue(bufferCarriesSound(ByteArray(62) + byteArrayOf(2, 0), 64))
        assertFalse(bufferCarriesSound(ByteArray(64), 63))
    }

    @Test fun aSourceThatDeliversOnlyZerosIsRotated() {
        ShadowAudioRecord.setSourceProvider { record ->
            created.add(record)
            object : ShadowAudioRecord.AudioRecordSource {
                override fun readInByteArray(buffer: ByteArray, offset: Int, size: Int, blocking: Boolean): Int {
                    reads.incrementAndGet()
                    Thread.sleep(2)
                    java.util.Arrays.fill(buffer, offset, offset + size, 0.toByte())
                    return size
                }
            }
        }
        val next = MicrophoneUplink(config(), onDiagnostic = diagnostics::add,
            noAudioRestartMillis = 60_000, silentSourceRotateMillis = 120)
        uplink = next
        assertTrue(next.start())

        Thread.sleep(900)

        // The rebuilt recorder reads -3 on the JVM shadow (it captures fine on hardware, as
        // the F7 field report showed), so this asserts the rotation decision, not the rebuilt
        // recorder's capture: the all-zero source was left behind.
        assertTrue(
            diagnostics.joinToString("\n"),
            diagnostics.any { it.contains("reason=silent-source") },
        )
    }

    @Test fun aSourceThatDeliversSoundIsNeverRotated() {
        ShadowAudioRecord.setSourceProvider { record ->
            created.add(record)
            object : ShadowAudioRecord.AudioRecordSource {
                override fun readInByteArray(buffer: ByteArray, offset: Int, size: Int, blocking: Boolean): Int {
                    reads.incrementAndGet()
                    Thread.sleep(2)
                    java.util.Arrays.fill(buffer, offset, offset + size, 0.toByte())
                    // A quiet but real waveform: the smallest 16-bit sample.
                    if (size > 1) buffer[offset + 1] = 1
                    return size
                }
            }
        }
        val next = MicrophoneUplink(config(), onDiagnostic = diagnostics::add,
            noAudioRestartMillis = 60_000, silentSourceRotateMillis = 120)
        uplink = next
        assertTrue(next.start())

        Thread.sleep(700)

        assertTrue(diagnostics.joinToString("\n"), diagnostics.none { it.contains("silent-source") })
        assertEquals(1, created.distinct().size)
        assertEquals(AudioRecord.RECORDSTATE_RECORDING, created.first().recordingState)
    }

    @Test fun anUnreadableSourceIsRotatedAfterTheReadRestarts() {
        // Every read fails with ERROR_INVALID_OPERATION, as plain MIC does on the Feifan F7 in
        // call mode: the same-recorder restarts run first, then the next source is tried.
        ShadowAudioRecord.setSourceProvider { record ->
            created.add(record)
            object : ShadowAudioRecord.AudioRecordSource {
                override fun readInByteArray(buffer: ByteArray, offset: Int, size: Int, blocking: Boolean): Int {
                    reads.incrementAndGet()
                    return AudioRecord.ERROR_INVALID_OPERATION
                }
            }
        }
        val next = MicrophoneUplink(config(), onDiagnostic = diagnostics::add,
            noAudioRestartMillis = 60_000, silentSourceRotateMillis = 120)
        uplink = next
        assertTrue(next.start())

        Thread.sleep(800)

        val all = diagnostics.joinToString("\n")
        assertTrue(all, diagnostics.any { it.contains("reason=read-error") })
        assertTrue(all, diagnostics.any { it.contains("reason=read-error-source") })
    }

    private fun config() = MicrophoneConfig(
        audioType = "speechrecognition",
        sampleRate = 16_000,
        channels = 1,
        payloadType = 100,
        frameMillis = 20,
        host = InetAddress.getLoopbackAddress(),
        port = 9,
        key = ByteArray(32),
    )
}
