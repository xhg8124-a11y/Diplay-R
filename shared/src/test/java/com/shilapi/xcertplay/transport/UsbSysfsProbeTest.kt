package com.shilapi.xcertplay.transport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbSysfsProbeTest {
    @Test fun absentOutputStaysInconclusive() {
        assertNull(UsbSysfsProbe.parse(null))
    }

    @Test fun devicesAreKeptAndInterfaceEchoesDropped() {
        val finding = UsbSysfsProbe.parse(
            "usb1 1d6b:0002\r\n1-0:1.0 1d6b:0002\n1-2 05ac:12a8\n1-2:1.0 05ac:12a8\n",
        )!!
        assertEquals(listOf("1-2 05ac:12a8", "usb1 1d6b:0002"), finding.devices)
        assertTrue(finding.appleListed)
    }

    @Test fun nonAppleKernelDevicesAreNotAnAppleFinding() {
        val finding = UsbSysfsProbe.parse("usb1 1d6b:0002\n1-2 1a86:5512")!!
        assertEquals(listOf("1-2 1a86:5512", "usb1 1d6b:0002"), finding.devices)
        assertFalse(finding.appleListed)
    }

    @Test fun shellNoiseIsDropped() {
        val finding = UsbSysfsProbe.parse("\nsome error\n1-2 05ac:12a8 extra\n05ac:12a8\n")!!
        assertTrue(finding.devices.isEmpty())
        assertFalse(finding.appleListed)
    }
}
