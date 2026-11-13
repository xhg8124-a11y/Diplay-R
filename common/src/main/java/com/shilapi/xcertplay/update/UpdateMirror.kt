package com.shilapi.xcertplay.update

import android.content.Context

/**
 * Where update checks and downloads are routed. Mainland-China drivers often cannot reach
 * api.github.com or github.com directly; a gh-proxy style mirror forwards both by URL prefix.
 *
 * The catalog still parses and validates the GitHub URLs the API returns, and only the request
 * URL is prefixed. A mirror is a third-party service that can serve different bytes, so the
 * SHA-256 check on the downloaded APK stays the trust boundary for the file itself.
 */
internal enum class UpdateMirror(val label: String, private val prefix: String?) {
    GITHUB("GitHub", null),
    GH_PROXY("gh-proxy.com", "https://gh-proxy.com/"),
    ;

    /** [url] stays a GitHub URL; a mirror only rewrites the request address. */
    fun proxied(url: String): String = if (prefix == null) url else prefix + url

    companion object {
        private const val PREFS = "diplay_update_mirror"
        private const val KEY = "mirror"

        fun load(context: Context): UpdateMirror {
            val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            return entries.firstOrNull { it.name == stored } ?: GITHUB
        }

        fun save(context: Context, mirror: UpdateMirror) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, mirror.name).apply()
        }
    }
}
