package com.app.printf.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.app.printf.data.entity.Invoice
import com.app.printf.data.entity.InvoiceLineItem

data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId",
    )
    val lineItems: List<InvoiceLineItem>,
)
