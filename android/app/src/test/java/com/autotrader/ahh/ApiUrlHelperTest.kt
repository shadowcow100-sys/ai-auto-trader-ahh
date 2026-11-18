package com.autotrader.ahh

import com.autotrader.ahh.data.local.ConnectionPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUrlHelperTest {

    @Test
    fun normalizeUrl_addsHttpAndDefaultPortForBareIp() {
        assertEquals(
            "http://192.168.1.50:8080",
            ConnectionPreferences.normalizeUrl("192.168.1.50")
        )
    }

    @Test
    fun normalizeUrl_preservesExplicitPortAndTrimsSlash() {
        assertEquals(
            "http://192.168.1.50:9090",
            ConnectionPreferences.normalizeUrl("  http://192.168.1.50:9090/  ")
        )
    }

    @Test
    fun normalizeUrl_preservesHttpsWithoutAdding8080() {
        assertEquals(
            "https://my-pc.tailnet.ts.net",
            ConnectionPreferences.normalizeUrl("https://my-pc.tailnet.ts.net/")
        )
    }
}
