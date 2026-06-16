package com.app.printf.util

import kotlin.math.floor
import kotlin.math.round

object TaxConstants {
    const val STATE_SALE = "State Sale"
    const val INTERSTATE_SALE = "Interstate Sale"
    const val GST_RATE = 18.0
    const val CGST_SGST_RATE = 9.0
}

data class InvoiceTaxTotals(
    val taxableAmount: Double,
    val sgst: Double,
    val cgst: Double,
    val igst: Double,
    val totalGst: Double,
    val gross: Double,
    val rounding: Double,
    val grandTotal: Double,
)

object TaxCalculator {
    fun isInterstateSale(salesType: String): Boolean {
        val normalized = salesType.trim()
        return normalized.equals(TaxConstants.INTERSTATE_SALE, ignoreCase = true) ||
            normalized.contains("interstate", ignoreCase = true) ||
            normalized.contains("inter state", ignoreCase = true)
    }

    fun sgstAmount(taxableAmount: Double): Double = taxableAmount * TaxConstants.CGST_SGST_RATE / 100.0

    fun cgstAmount(taxableAmount: Double): Double = taxableAmount * TaxConstants.CGST_SGST_RATE / 100.0

    fun igstAmount(taxableAmount: Double): Double = taxableAmount * TaxConstants.GST_RATE / 100.0

    fun roundIndianRupee(amount: Double): Double {
        val normalized = round(amount * 100.0) / 100.0
        val rupees = floor(normalized)
        val paisa = round((normalized - rupees) * 100.0).toInt()
        return if (paisa >= 50) rupees + 1.0 else rupees
    }

    fun invoiceTotals(taxableAmount: Double, isInterstate: Boolean): InvoiceTaxTotals {
        val sgst = if (isInterstate) 0.0 else sgstAmount(taxableAmount)
        val cgst = if (isInterstate) 0.0 else cgstAmount(taxableAmount)
        val igst = if (isInterstate) igstAmount(taxableAmount) else 0.0
        val totalGst = sgst + cgst + igst
        val gross = taxableAmount + totalGst
        val grandTotal = roundIndianRupee(gross)
        val rounding = grandTotal - gross
        return InvoiceTaxTotals(
            taxableAmount = taxableAmount,
            sgst = sgst,
            cgst = cgst,
            igst = igst,
            totalGst = totalGst,
            gross = gross,
            rounding = rounding,
            grandTotal = grandTotal,
        )
    }
}
