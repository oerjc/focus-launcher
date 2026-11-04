package com.focuslauncher.app

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable
) {
    val componentKey: String get() = "$packageName/$activityName"
}
