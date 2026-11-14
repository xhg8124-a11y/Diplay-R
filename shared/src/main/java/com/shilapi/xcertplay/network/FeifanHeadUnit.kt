package com.shilapi.xcertplay.network

import android.content.Context
import android.os.Build

/**
 * Identifies the Feifan (飞凡, SAIC) RISING OS head-unit family, written for the Feifan F7:
 * Qualcomm SA8155, a 15.05" 2.5K centre screen and a 12.3" 1920x1080 passenger screen.
 *
 * Unlike the Geely and BYD gates, no F7 has been measured yet — the identifiers below come from
 * SAIC's public branding (risingauto.com), not from a confirmed unit. Detection therefore only
 * suggests the preset; the driver confirms it under Settings → Vehicle, following the
 * manual-confirmation pattern of `setup.DiLinkGeneration`. `rising` alone never matches, because
 * phone ROMs named RisingOS exist that have nothing to do with SAIC.
 */
object FeifanHeadUnit {

    /** Manual choice, stored next to the setup-guide preferences. */
    enum class Preset { AUTO, ON, OFF }

    /**
     * Physical width of the F7's 15.05" 16:10 centre screen: 382 mm diagonal is 324 mm wide,
     * snapped to the 5 mm physical-size grid. iOS sizes its CarPlay controls from this number.
     */
    const val F7_WIDTH_PHYSICAL_MM = 325

    private const val PREFS = "diplay"
    private const val KEY_PRESET = "feifan_preset"

    private val identifiers: List<String>
        get() = runCatching {
            listOf(Build.PRODUCT, Build.DEVICE, Build.MODEL, Build.MANUFACTURER, Build.BRAND)
                .map { it.lowercase() }
        }.getOrDefault(emptyList())

    /** Detection rule, separate from [detected] so tests cover it without touching `Build`. */
    fun matchesIdentifiers(identifiers: List<String>): Boolean {
        val ids = identifiers.map { it.lowercase() }
        fun has(needle: String) = ids.any { it.contains(needle) }
        // Measured on an F7: fingerprint risingauto/ep35/ep35:9/..., product/device ep35,
        // Android 9. The exact product match survives a rebranding that drops "risingauto".
        return has("feifan") || has("risingauto") || ids.any { it == "ep35" } ||
            (has("saic") && has("rising"))
    }

    /** Best-effort Build-based detection; a phone ROM named RisingOS alone does not match. */
    val detected: Boolean = matchesIdentifiers(identifiers)

    fun preset(context: Context): Preset = when (prefs(context).getString(KEY_PRESET, null)) {
        "on" -> Preset.ON
        "off" -> Preset.OFF
        else -> Preset.AUTO
    }

    fun savePreset(context: Context, preset: Preset) {
        prefs(context).edit().putString(KEY_PRESET, preset.name.lowercase()).apply()
    }

    /** The Feifan tuning applies when the driver turned it on, or on detection when left on automatic. */
    fun active(context: Context): Boolean = when (preset(context)) {
        Preset.ON -> true
        Preset.OFF -> false
        Preset.AUTO -> detected
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
