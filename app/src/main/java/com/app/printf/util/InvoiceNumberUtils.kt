package com.app.printf.util

import java.util.Calendar

object InvoiceNumberUtils {
    private const val MAX_LENGTH = 24

    fun sanitizeInput(input: String): String {
        return input
            .filter { it.isLetterOrDigit() || it == '-' || it == '/' }
            .take(MAX_LENGTH)
            .uppercase()
    }

    fun formatForPdf(invoiceNumber: String): String {
        val trimmed = invoiceNumber.trim().uppercase()
        if (trimmed.isBlank()) return "-"
        return if (trimmed.any { it.isLetter() }) {
            trimmed
        } else {
            "PR-${trimmed.padStart(4, '0')}"
        }
    }

    fun sanitizeForFileName(invoiceNumber: String): String {
        return invoiceNumber
            .trim()
            .ifBlank { "draft" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    fun financialYearPrefix(dateMillis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val year = calendar.get(Calendar.YEAR)
        val startYear = if (calendar.get(Calendar.MONTH) >= Calendar.APRIL) {
            year
        } else {
            year - 1
        }
        val endYearShort = (startYear + 1) % 100
        return "PR-$startYear/${endYearShort.toString().padStart(2, '0')}"
    }

    fun suggestNext(existingNumbers: List<String>, dateMillis: Long = System.currentTimeMillis()): String {
        val prefix = financialYearPrefix(dateMillis)
        val maxSequence = existingNumbers.mapNotNull { value ->
            sequenceForPrefix(value, prefix)
        }.maxOrNull() ?: 0
        return formatInvoiceNumber(prefix, maxSequence + 1)
    }

    fun suggestNextAfter(
        existingNumbers: List<String>,
        lastUsedNumber: String,
        dateMillis: Long = System.currentTimeMillis(),
    ): String {
        val prefix = financialYearPrefix(dateMillis)
        val lastUsedSequence = sequenceForPrefix(lastUsedNumber, prefix)
        val maxSequence = maxOf(
            existingNumbers.mapNotNull { sequenceForPrefix(it, prefix) }.maxOrNull() ?: 0,
            lastUsedSequence ?: 0,
        )
        val next = formatInvoiceNumber(prefix, maxSequence + 1)
        return if (next == lastUsedNumber.trim().uppercase()) {
            formatInvoiceNumber(prefix, maxSequence + 2)
        } else {
            next
        }
    }

    private fun sequenceForPrefix(invoiceNumber: String, prefix: String): Int? {
        val trimmed = invoiceNumber.trim().uppercase()
        if (!trimmed.startsWith("$prefix-")) return null
        return trimmed.removePrefix("$prefix-").toIntOrNull()
    }

    private fun formatInvoiceNumber(prefix: String, sequence: Int): String {
        return "$prefix-${sequence.toString().padStart(3, '0')}"
    }
}
