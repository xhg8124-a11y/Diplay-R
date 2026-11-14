package com.shilapi.xcertplay.network

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class FeifanHeadUnitTest {
    private lateinit var context: Context

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("diplay", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun matchesTheSaicRisingBuildIdentifiers() {
        assertTrue(FeifanHeadUnit.matchesIdentifiers(listOf("saic", "rising_os", "f7")))
    }

    @Test fun matchesTheMeasuredEp35HeadUnit() {
        // Reported by an F7: fingerprint risingauto/ep35/ep35:9/PQ3B.190801.002.
        assertTrue(FeifanHeadUnit.matchesIdentifiers(listOf("ep35", "ep35", "qti_ep35")))
        // Only the exact product code counts; "ep35x" boards of other brands must not match.
        assertFalse(FeifanHeadUnit.matchesIdentifiers(listOf("ep35x", "other")))
    }

    @Test fun matchesTheFeifanBrandAlone() {
        assertTrue(FeifanHeadUnit.matchesIdentifiers(listOf("feifan", "f7", "saic")))
    }

    @Test fun matchesTheRisingAutoDomain() {
        assertTrue(FeifanHeadUnit.matchesIdentifiers(listOf("risingauto", "ivi")))
    }

    @Test fun ignoresPhoneRomsNamedRising() {
        // RisingOS is a phone custom ROM with no SAIC markers; "rising" alone must not match.
        assertFalse(FeifanHeadUnit.matchesIdentifiers(listOf("xiaomi", "rising")))
    }

    @Test fun ignoresOtherHeadUnits() {
        assertFalse(FeifanHeadUnit.matchesIdentifiers(listOf("byd-auto", "dilink5.0")))
        assertFalse(FeifanHeadUnit.matchesIdentifiers(listOf("geely_fx11", "cs11_a4", "msmnile_gvmq")))
    }

    @Test fun automaticFollowsDetectionAndARoboticBuildIsNotAFeifan() {
        assertEquals(FeifanHeadUnit.Preset.AUTO, FeifanHeadUnit.preset(context))
        assertFalse(FeifanHeadUnit.detected)
        assertFalse(FeifanHeadUnit.active(context))
    }

    @Test fun theManualChoiceWinsOverDetection() {
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.ON)
        assertTrue(FeifanHeadUnit.active(context))
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.OFF)
        assertFalse(FeifanHeadUnit.active(context))
        FeifanHeadUnit.savePreset(context, FeifanHeadUnit.Preset.AUTO)
        assertFalse(FeifanHeadUnit.active(context))
    }
}
