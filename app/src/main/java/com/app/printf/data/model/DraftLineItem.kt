package com.app.printf.data.model

import com.app.printf.data.entity.Product

data class DraftLineItem(
    val product: Product,
    val quantity: Int,
) {
    val lineTotal: Double get() = product.price * quantity
}
