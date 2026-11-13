package com.shilapi.xcertplay.update

import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric provides the real org.json that UpdateCatalog parses with.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], manifest = Config.NONE)
class UpdateReleaseLookupTest {
    private lateinit var server: HttpServer
    private lateinit var root: String
    private val mirrorHits = AtomicInteger()
    private val directHits = AtomicInteger()

    private val releasesJson = """
        [{"tag_name": "v0.2.24-r1", "draft": false, "assets": [
            {"name": "Diplay-R-0.2.24-r1.apk",
             "browser_download_url": "https://github.com/xhg8124-a11y/Diplay-R/releases/download/v0.2.24-r1/Diplay-R-0.2.24-r1.apk"},
            {"name": "SHA256SUMS.txt",
             "browser_download_url": "https://github.com/xhg8124-a11y/Diplay-R/releases/download/v0.2.24-r1/SHA256SUMS.txt"}
        ]}]
    """.trimIndent()

    @Before fun startServer() {
        server = HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0)
        server.executor = Executors.newSingleThreadExecutor()
        root = "http://${server.address.hostString}:${server.address.port}"
        server.start()
    }

    @After fun stopServer() {
        server.stop(0)
    }

    /** Serves [body] at [path] and counts the requests into [counter]. */
    private fun respond(path: String, status: Int, body: String, counter: AtomicInteger) {
        server.createContext(path) { exchange ->
            counter.incrementAndGet()
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, if (status in 200..299) bytes.size.toLong() else -1)
            if (status in 200..299) exchange.responseBody.use { it.write(bytes) }
        }
    }

    /** Rewrites the real route URLs onto the loopback server paths. */
    private fun mapRoutes(mirrorPath: String, directPath: String): (URL) -> URLConnection = { url ->
        when {
            url.toString().startsWith("https://gh-proxy.com/") -> URL(url.toString().replace("https://gh-proxy.com/https://api.github.com", "$root/$mirrorPath"))
            url.host == "api.github.com" -> URL(url.toString().replace("https://api.github.com", "$root/$directPath"))
            else -> url
        }.openConnection()
    }

    @Test fun aHealthyMirrorAnswersTheCheckAlone() {
        respond("/mirror-ok", 200, releasesJson, mirrorHits)
        respond("/direct-ok", 200, releasesJson, directHits)
        val release = UpdateReleaseLookup.latest(
            installedVersion = "0.2.23-r1",
            userAgent = "DiPlay/test",
            openConnection = mapRoutes("mirror-ok", "direct-ok"),
            mirror = UpdateMirror.GH_PROXY,
        )!!
        assertEquals("v0.2.24-r1", release.tagName)
        assertEquals(1, mirrorHits.get())
        assertEquals(0, directHits.get())
    }

    @Test fun anExhaustedMirrorQuotaFallsBackToDirectGitHub() {
        // The observed gh-proxy.com failure: GitHub's own 403 rate-limit body, passed through.
        respond("/mirror-403", 403, """{"message": "API rate limit exceeded"}""", mirrorHits)
        respond("/direct-ok", 200, releasesJson, directHits)
        val release = UpdateReleaseLookup.latest(
            installedVersion = "0.2.23-r1",
            userAgent = "DiPlay/test",
            openConnection = mapRoutes("mirror-403", "direct-ok"),
            mirror = UpdateMirror.GH_PROXY,
        )!!
        assertEquals("v0.2.24-r1", release.tagName)
        assertEquals(1, mirrorHits.get())
        assertEquals(1, directHits.get())
    }

    @Test fun aFailedCheckOnBothRoutesReportsTheFailure() {
        respond("/mirror-403", 403, """{"message": "API rate limit exceeded"}""", mirrorHits)
        respond("/direct-403", 403, """{"message": "API rate limit exceeded"}""", directHits)
        assertThrows(IOException::class.java) {
            UpdateReleaseLookup.latest(
                installedVersion = "0.2.23-r1",
                userAgent = "DiPlay/test",
                openConnection = mapRoutes("mirror-403", "direct-403"),
                mirror = UpdateMirror.GH_PROXY,
            )
        }
    }

    @Test fun theDirectChoiceStillQueriesOnlyDirectGitHub() {
        respond("/mirror-ok", 200, releasesJson, mirrorHits)
        respond("/direct-ok", 200, releasesJson, directHits)
        UpdateReleaseLookup.latest(
            installedVersion = "0.2.23-r1",
            userAgent = "DiPlay/test",
            openConnection = mapRoutes("mirror-ok", "direct-ok"),
            mirror = UpdateMirror.GITHUB,
        )
        assertEquals(0, mirrorHits.get())
        assertEquals(1, directHits.get())
    }

    @Test fun anUpToDateMirrorAnswerStopsBeforeTheFallback() {
        respond("/mirror-current", 200, releasesJson, mirrorHits)
        respond("/direct-ok", 200, releasesJson, directHits)
        val release = UpdateReleaseLookup.latest(
            installedVersion = "0.2.24-r1",
            userAgent = "DiPlay/test",
            openConnection = mapRoutes("mirror-current", "direct-ok"),
            mirror = UpdateMirror.GH_PROXY,
        )
        assertEquals(null, release)
        assertEquals(1, mirrorHits.get())
        assertEquals(0, directHits.get())
    }
}
