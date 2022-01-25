package com.google.sample.cast.refplayer.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.sample.cast.refplayer.R

/**
 * Formats time from milliseconds to hh:mm:ss string format.
 */
fun Int.formatMillis(): String {
    var seconds = (this / 1000)
    val hours = seconds / (60 * 60)
    seconds %= 60 * 60
    val minutes = seconds / 60
    seconds %= 60

    val time: String = if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
    return time
}

fun View.flipVisibility() {
    visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
}

fun View.setVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}


@SuppressWarnings("deprecation")
/**
 * Returns the screen/display size
 */
fun Activity.getDisplaySize(): Point {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val width = display.width
    val height = display.height
    return Point(width, height)
}

/**
 * Returns {@code true} if and only if the screen orientation is portrait.
 */
fun Activity.isOrientationPortrait(): Boolean =
    resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

/**
 * Shows an error dialog with a given text message.
 */
fun Activity.showErrorDialog(errorString: String) {
    AlertDialog.Builder(this).setTitle(R.string.error)
        .setMessage(errorString)
        .setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
        .create()
        .show()
}

/**
 * Shows an "Oops" error dialog with a text provided by a resource ID
 */
fun Activity.showOopsDialog(resourceId: Int) {
    AlertDialog.Builder(this).setTitle(R.string.oops)
        .setMessage(getString(resourceId))
        .setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
        .setIcon(R.drawable.ic_action_alerts_and_states_warning)
        .create()
        .show()
}

/**
 * Gets the version of app.
 */
fun Activity.getAppVersionName(): String {
    var versionString: String? = null
    try {
        val info: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        versionString = info.versionName
    } catch (e: Exception) {
        // do nothing
    }
    return versionString!!
}

/**
 * Shows a (long) toast.
 */
fun Activity.showToast(resourceId: Int) {
    Toast.makeText(this, getString(resourceId), Toast.LENGTH_LONG).show()
}