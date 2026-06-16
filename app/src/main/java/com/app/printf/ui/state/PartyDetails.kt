package com.app.printf.ui.state

data class PartyDetails(
    val name: String = "",
    val address: String = "",
    val gstin: String = "",
    val mobile: String = "",
) {
    val isFilled: Boolean get() = name.isNotBlank() && address.isNotBlank()
}
