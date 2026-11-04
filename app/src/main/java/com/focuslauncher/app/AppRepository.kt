package com.focuslauncher.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

object AppRepository {

    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        val self = context.packageName
        return resolved
            .asSequence()
            .filter { it.activityInfo.packageName != self }
            .map {
                val ai = it.activityInfo
                AppInfo(
                    packageName = ai.packageName,
                    activityName = ai.name,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun resolveDialerIntent(context: Context): Intent? {
        val dial = Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return if (dial.resolveActivity(context.packageManager) != null) dial else null
    }

    fun resolveSettingsIntent(): Intent {
        return Intent(android.provider.Settings.ACTION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun resolveHomeChooser(context: Context): Intent {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return Intent.createChooser(intent, context.getString(R.string.exit_launcher))
    }

    fun openDefaultAppsSettings(@Suppress("UNUSED_PARAMETER") context: Context): Intent {
        return Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
