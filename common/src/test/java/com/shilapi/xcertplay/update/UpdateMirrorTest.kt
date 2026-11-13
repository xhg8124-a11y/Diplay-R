package com.shilapi.xcertplay.update

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class UpdateMirrorTest {
    private lateinit var context: Context

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("diplay_update_mirror", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun directConnectionLeavesUrlsUntouched() {
        assertEquals(
            "https://api.github.com/repos/xhg8124-a11y/Diplay-R/releases?per_page=3",
            UpdateMirror.GITHUB.proxied("https://api.github.com/repos/xhg8124-a11y/Diplay-R/releases?per_page=3"),
        )
    }

    @Test fun theMirrorOnlyPrefixesTheRequestAddress() {
        val github = "https://github.com/xhg8124-a11y/Diplay-R/releases/download/v0.2.23-r1/DiPlay.apk"
        assertEquals("https://gh-proxy.com/$github", UpdateMirror.GH_PROXY.proxied(github))
    }

    @Test fun defaultsToDirectAndRoundTripsAChoice() {
        assertEquals(UpdateMirror.GITHUB, UpdateMirror.load(context))
        UpdateMirror.save(context, UpdateMirror.GH_PROXY)
        assertEquals(UpdateMirror.GH_PROXY, UpdateMirror.load(context))
    }

    @Test fun aStoredValueThatIsNoLongerAMirrorFallsBackToDirect() {
        context.getSharedPreferences("diplay_update_mirror", Context.MODE_PRIVATE)
            .edit().putString("mirror", "DEAD_MIRROR").commit()
        assertEquals(UpdateMirror.GITHUB, UpdateMirror.load(context))
    }
}
