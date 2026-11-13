package com.shilapi.xcertplay.transport

import android.content.Context
import com.shilapi.xcertplay.adb.AdbKeys
import com.shilapi.xcertplay.adb.LocalAdb

/**
 * Reads the kernel's USB device table over the head unit's own ADB while the framework lists no
 * iPhone, one probe per wired wait.
 *
 * This separates the two "iPhone not listed" families that need opposite user action: when the
 * kernel sees nothing either, the port or cable carries no data; when the kernel lists the Apple
 * device but [android.hardware.usb.UsbManager] does not, the head unit's framework hides it from
 * applications and no cable change will help. Best effort only: without an approved local ADB the
 * probe reports unavailable and the caller keeps its own framework-side evidence.
 */
internal object UsbSysfsProbe {

    /** [IphoneUsbMatcher.APPLE_VENDOR_ID] as sysfs writes it, lowercase hex. */
    internal const val APPLE_SYSFS_VENDOR = "05ac"

    /** Lists device entries with a vendor id; interface entries (names with a colon) stay out. */
    internal const val COMMAND =
        "for d in /sys/bus/usb/devices/*; do [ -f \"\$d/idVendor\" ] && " +
            "echo \"\${d##*/} \$(cat \"\$d/idVendor\"):\$(cat \"\$d/idProduct\")\"; done"

    data class Finding(
        val devices: List<String>,
        val appleListed: Boolean,
    )

    /** Returns null when local ADB is not approved or reachable: inconclusive, not "no device". */
    fun probe(context: Context): Finding? {
        val app = context.applicationContext
        return runCatching {
            LocalAdb(AdbKeys.load(app)).use { adb ->
                // Never offer a new key: a wired wait must not trigger an ADB approval prompt.
                if (adb.connect(mayAsk = false) != LocalAdb.Access.READY) return@use null
                parse(adb.shell(COMMAND, 5_000))
            }
        }.getOrNull()
    }

    /** Keeps `name vid:pid` lines for USB devices, dropping hubs' interface echoes and noise. */
    internal fun parse(output: String?): Finding? {
        if (output == null) return null
        val devices = output.lineSequence()
            .map { it.trim() }
            .filter { line -> Regex("^[0-9a-zA-Z.\\-]+ [0-9a-fA-F]{4}:[0-9a-fA-F]{4}$").matches(line) }
            .distinct()
            .sorted()
            .toList()
        return Finding(devices, devices.any { it.substringAfter(' ').startsWith("$APPLE_SYSFS_VENDOR:", ignoreCase = true) })
    }
}
