package com.google.sample.cast.refplayer.utils

import android.view.View
import androidx.core.view.isVisible

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