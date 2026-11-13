package com.shilapi.xcertplay.update

import java.io.IOException
import java.net.URL
import java.net.URLConnection

internal object UpdateReleaseLookup {
    /**
     * A mirror's API forwarding rides on shared GitHub quotas that run dry intermittently
     * (observed 403 "API rate limit exceeded" from gh-proxy.com, back within the same hour),
     * so a chosen mirror falls back to direct GitHub for the check. Downloads keep the mirror:
     * the object proxy path is unaffected by the API quota.
     */
    fun latest(
        installedVersion: String,
        userAgent: String,
        openConnection: (URL) -> URLConnection = { it.openConnection() },
        mirror: UpdateMirror = UpdateMirror.GITHUB,
    ): UpdateRelease? {
        val routes = if (mirror == UpdateMirror.GITHUB) {
            listOf(mirror)
        } else {
            listOf(mirror, UpdateMirror.GITHUB)
        }
        var lastFailure: IOException? = null
        for (route in routes) {
            try {
                val json = UpdateClient.fetchText(
                    route.proxied(UpdateClient.RELEASES_URL),
                    "application/vnd.github+json",
                    userAgent,
                    openConnection,
                )
                val release = UpdateCatalog.parse(json) ?: return null
                return release.takeIf { UpdateVersion.isNewer(it.tagName, installedVersion) }
            } catch (failure: IOException) {
                lastFailure = failure
            }
        }
        throw lastFailure ?: IOException("No update route was available")
    }
}
