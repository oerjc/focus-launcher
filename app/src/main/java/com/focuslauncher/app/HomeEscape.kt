package com.focuslauncher.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/**
 * Opens the system screen where the user picks the default home app.
 * No biometric. The first intent that starts wins.
 */
object HomeEscape {

    fun openSystemHomeSettings(context: Context) {
        val primary = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        )
        val fallback = listOf(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            },
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in primary) {
            if (tryStart(context, intent)) return
        }
        for (intent in fallback) {
            if (tryStart(context, intent)) {
                Toast.makeText(context, R.string.restore_opened_fallback, Toast.LENGTH_LONG).show()
                return
            }
        }
        Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_LONG).show()
    }

    private fun tryStart(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: Exception) {
            false
        }
    }
}
