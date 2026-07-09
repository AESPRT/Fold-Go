package com.aesprt.foldgo.core.util

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {
    private val phLocale = Locale("en", "PH")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(phLocale)

    fun format(amount: Double): String {
        return currencyFormatter.format(amount).replace("PHP", "₱")
    }
}
