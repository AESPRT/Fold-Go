package com.aesprt.foldgo.core.util

import java.util.Locale

object KgFormatterUtils {
    fun formatDouble(weightInAction: Double, weightFinished: Double): String {
        return String.format(
            Locale.getDefault(),
            "%.1f",
            weightInAction + weightFinished
        )
    }

    fun formatDouble(weight: Double): String {
        return String.format(
            Locale.getDefault(),
            "%.1f",
            weight
        )
    }
}