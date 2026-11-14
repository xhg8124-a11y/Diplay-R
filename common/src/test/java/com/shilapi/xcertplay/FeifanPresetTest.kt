package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.airplay.CarPlaySize
import com.shilapi.xcertplay.network.FeifanHeadUnit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], manifest = Config.NONE)
class FeifanPresetTest {
    private lateinit var context: Context

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("diplay", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("xcertplay_airplay", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun thePresetSizesTheCentreScreenAtItsRealWidth() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        assertEquals(FeifanHeadUnit.F7_WIDTH_PHYSICAL_MM, AirPlayPersistence.loadWidthPhysicalMm(context))
    }

    @Test fun withoutThePresetTheStockWidthStays() {
        assertEquals(CarPlaySize.DEFAULT.widthMillimeters, AirPlayPersistence.loadWidthPhysicalMm(context))
    }

    @Test fun aDriverChosenWidthBeatsThePreset() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        AirPlayPersistence.saveWidthPhysicalMm(context, 250)
        assertEquals(250, AirPlayPersistence.loadWidthPhysicalMm(context))
    }

    @Test fun turningThePresetOffRestoresTheStockDefault() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.OFF)
        assertEquals(CarPlaySize.DEFAULT.widthMillimeters, AirPlayPersistence.loadWidthPhysicalMm(context))
    }

    @Test fun thePresetStartsAnUnchosenWirelessMusicBufferAt500Ms() {
        // 500 ms held the F7's 250–460 ms radio gaps; a 1000 ms start level made the sender's
        // first /feedback read "received 750 ms, played 0" and restart the stream in a loop.
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        assertEquals(500, AirPlayPersistence.loadMediaBufferMillis(context, wireless = true))
    }

    @Test fun aDriverChosenMusicBufferBeatsThePresetDefault() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        AirPlayPersistence.saveMediaBufferMillis(context, wireless = true, 500)
        assertEquals(500, AirPlayPersistence.loadMediaBufferMillis(context, wireless = true))
    }

    @Test fun thePresetTurnsOnTheAudioFocusPair() {
        // Field-tested on the F7: with focus on, prompts claim it while speaking and the
        // volume knob follows them; calls duck the music through auto-yield (default on).
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        assertEquals(true, AirPlayPersistence.loadAudioFocusEnabled(context))
        assertEquals(true, AirPlayPersistence.loadNavigationPromptAudioFocus(context))
        assertEquals(true, AirPlayPersistence.loadAudioFocusAutoYield(context))
    }

    @Test fun aDriverChosenFocusChoiceBeatsThePresetDefaults() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        AirPlayPersistence.saveAudioFocusEnabled(context, false)
        AirPlayPersistence.saveNavigationPromptAudioFocus(context, false)
        assertEquals(false, AirPlayPersistence.loadAudioFocusEnabled(context))
        assertEquals(false, AirPlayPersistence.loadNavigationPromptAudioFocus(context))
    }

    @Test fun withoutThePresetTheFocusTogglesStayOptIn() {
        assertEquals(false, AirPlayPersistence.loadAudioFocusEnabled(context))
        assertEquals(false, AirPlayPersistence.loadNavigationPromptAudioFocus(context))
    }

    @Test fun withoutThePresetTheStockWirelessBufferStays() {
        assertEquals(300, AirPlayPersistence.loadMediaBufferMillis(context, wireless = true))
    }
}
