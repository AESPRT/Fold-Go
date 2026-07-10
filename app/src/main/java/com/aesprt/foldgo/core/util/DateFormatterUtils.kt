package com.aesprt.foldgo.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatterUtils {
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}