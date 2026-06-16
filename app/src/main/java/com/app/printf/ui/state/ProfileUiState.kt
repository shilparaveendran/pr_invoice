package com.app.printf.ui.state

enum class SignatureInputMode {
    DRAW,
    IMAGE,
}

data class ProfileUiState(
    val companyName: String = "",
    val address: String = "",
    val gstin: String = "",
    val pan: String = "",
    val phone: String = "",
    val email: String = "",
    val salesType: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val accountType: String = "",
    val bankName: String = "",
    val signaturePath: String = "",
    val signatureCleared: Boolean = false,
    val signatureInputMode: SignatureInputMode = SignatureInputMode.DRAW,
    val signatureRevision: Int = 0,
    val companySectionExpanded: Boolean = false,
    val bankSectionExpanded: Boolean = false,
    val signatureSectionExpanded: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

