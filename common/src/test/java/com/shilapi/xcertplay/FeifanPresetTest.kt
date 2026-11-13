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
}
