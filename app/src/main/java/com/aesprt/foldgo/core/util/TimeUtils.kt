package com.aesprt.foldgo.core.util

import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    /**
     * Formats a duration in milliseconds into a string like "MM:SS remaining"
     */
    fun formatRemainingTime(remainingMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d remaining", minutes, seconds)
    }

    /**
     * Formats a duration in milliseconds into a string like "MM:SS"
     */
    fun formatDuration(durationMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}