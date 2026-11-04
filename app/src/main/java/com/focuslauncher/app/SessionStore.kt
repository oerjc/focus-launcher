package com.focuslauncher.app

import android.content.Context
import android.os.SystemClock

/**
 * Persists the current-session allowlist.
 * Reboot detection: SystemClock.elapsedRealtime() resets after reboot, so if
 * current elapsed is less than the value saved at session start, treat as reboot.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun hasActiveSession(): Boolean {
        if (!prefs.getBoolean(KEY_ACTIVE, false)) return false
        val savedElapsed = prefs.getLong(KEY_ELAPSED_AT_START, -1L)
        if (savedElapsed < 0L) return false
        val now = SystemClock.elapsedRealtime()
        // After reboot, elapsedRealtime restarts near 0 and will be < saved value
        if (now < savedElapsed) {
            clear()
            return false
        }
        return true
    }

    fun getAllowedPackages(): Set<String> {
        if (!hasActiveSession()) return emptySet()
        return prefs.getStringSet(KEY_PACKAGES, emptySet())?.toSet() ?: emptySet()
    }

    fun saveSession(packages: Set<String>) {
        prefs.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putStringSet(KEY_PACKAGES, packages)
            .putLong(KEY_ELAPSED_AT_START, SystemClock.elapsedRealtime())
            .putLong(KEY_WALL_AT_START, System.currentTimeMillis())
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    /** Called from BootReceiver — force re-selection after reboot. */
    fun clearOnBoot() {
        clear()
    }

    companion object {
        private const val PREFS = "focus_session"
        private const val KEY_ACTIVE = "active"
        private const val KEY_PACKAGES = "packages"
        private const val KEY_ELAPSED_AT_START = "elapsed_at_start"
        private const val KEY_WALL_AT_START = "wall_at_start"
    }
}
