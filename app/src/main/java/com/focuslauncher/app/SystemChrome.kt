package com.focuslauncher.app

import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.max

/**
 * Keeps the system status and navigation bars visible and leaves the edge-gesture
 * zones free of this app's controls.
 *
 * A third-party HOME cannot repair OEM gesture services that live inside the stock
 * launcher. It can avoid making that worse: no immersive / sticky-hide flags, no
 * gesture-exclusion rectangles, and content padded inside system bar, cutout, and
 * system-gesture insets.
 */
object SystemChrome {

    fun install(activity: ComponentActivity, root: View) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        showSystemBars(activity)

        val baseLeft = root.paddingLeft
        val baseTop = root.paddingTop
        val baseRight = root.paddingRight
        val baseBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val gestures = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                baseLeft + max(bars.left, gestures.left),
                baseTop + max(bars.top, gestures.top),
                baseRight + max(bars.right, gestures.right),
                baseBottom + maxOf(bars.bottom, gestures.bottom, ime.bottom)
            )
            clearGestureExclusion(v)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }

    fun showSystemBars(activity: ComponentActivity) {
        val window = activity.window
        @Suppress("DEPRECATION")
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        clearGestureExclusion(window.decorView)
    }

    private fun clearGestureExclusion(view: View) {
        ViewCompat.setSystemGestureExclusionRects(view, emptyList())
    }
}
