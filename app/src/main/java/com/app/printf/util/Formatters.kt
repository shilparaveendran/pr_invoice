package com.app.printf.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val pdfAmountFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

    fun formatPdfAmount(amount: Double): String {
        val formatted = pdfAmountFormat.format(amount)
        return if (formatted.startsWith("-")) {
            "-₹${formatted.removePrefix("-")}"
        } else {
            "₹$formatted"
        }
    }

    fun formatDate(millis: Long): String {
        val date = Date(millis)
        val day = SimpleDateFormat("d", Locale.getDefault()).format(date)
        val month = SimpleDateFormat("MMM", Locale.ENGLISH).format(date).lowercase(Locale.getDefault())
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        return "$day-$month-$year"
    }
}
