package com.app.printf.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.printf.data.entity.CompanyProfile
import com.app.printf.domain.repository.CompanyProfileRepository
import android.net.Uri
import com.app.printf.ui.state.ProfileUiState
import com.app.printf.ui.state.SignatureInputMode
import com.app.printf.util.SignatureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val companyProfileRepository: CompanyProfileRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { companyProfileRepository.getProfile() }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            companyName = profile.companyName,
                            address = profile.address,
                            gstin = profile.gstin,
                            pan = profile.pan,
                            phone = profile.phone,
                            email = profile.email,
                            salesType = profile.salesType,
                            accountHolderName = profile.accountHolderName,
                            accountNumber = profile.accountNumber,
                            ifscCode = profile.ifscCode,
                            accountType = profile.accountType,
                            bankName = profile.bankName,
                            signaturePath = profile.signaturePath,
                            signatureCleared = false,
                            signatureInputMode = if (profile.signaturePath.isNotBlank()) {
                                SignatureInputMode.IMAGE
                            } else {
                                SignatureInputMode.DRAW
                            },
                            isLoading = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun onCompanyNameChange(value: String) {
        _uiState.update { it.copy(companyName = value, errorMessage = null, successMessage = null) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, errorMessage = null, successMessage = null) }
    }

    fun onGstinChange(value: String) {
        _uiState.update { it.copy(gstin = value, errorMessage = null, successMessage = null) }
    }

    fun onPanChange(value: String) {
        _uiState.update { it.copy(pan = value, errorMessage = null, successMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null, successMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun onSalesTypeChange(value: String) {
        _uiState.update { it.copy(salesType = value, errorMessage = null, successMessage = null) }
    }

    fun onAccountHolderNameChange(value: String) {
        _uiState.update { it.copy(accountHolderName = value, errorMessage = null, successMessage = null) }
    }

    fun onAccountNumberChange(value: String) {
        _uiState.update { it.copy(accountNumber = value, errorMessage = null, successMessage = null) }
    }

    fun onIfscCodeChange(value: String) {
        _uiState.update { it.copy(ifscCode = value, errorMessage = null, successMessage = null) }
    }

    fun onAccountTypeChange(value: String) {
        _uiState.update { it.copy(accountType = value, errorMessage = null, successMessage = null) }
    }

    fun onBankNameChange(value: String) {
        _uiState.update { it.copy(bankName = value, errorMessage = null, successMessage = null) }
    }

    fun setCompanySectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(companySectionExpanded = expanded) }
    }

    fun setBankSectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(bankSectionExpanded = expanded) }
    }

    fun setSignatureSectionExpanded(expanded: Boolean) {
        _uiState.update { it.copy(signatureSectionExpanded = expanded) }
    }

    fun setSignatureInputMode(mode: SignatureInputMode) {
        _uiState.update { it.copy(signatureInputMode = mode, errorMessage = null) }
    }

    fun markSignatureCleared() {
        val path = _uiState.value.signaturePath
        if (path.isNotBlank()) {
            SignatureStorage.delete(path)
        }
        _uiState.update {
            it.copy(
                signatureCleared = true,
                signaturePath = "",
                signatureRevision = it.signatureRevision + 1,
                successMessage = null,
            )
        }
    }

    fun onSignatureImageUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                SignatureStorage.loadFromUri(appContext, uri)
            }
            if (bitmap == null) {
                _uiState.update { it.copy(errorMessage = "Could not load image") }
                return@launch
            }
            val state = _uiState.value
            if (state.signaturePath.isNotBlank()) {
                SignatureStorage.delete(state.signaturePath)
            }
            val path = withContext(Dispatchers.IO) {
                SignatureStorage.save(appContext, bitmap)
            }
            _uiState.update {
                it.copy(
                    signaturePath = path,
                    signatureCleared = false,
                    signatureInputMode = SignatureInputMode.IMAGE,
                    signatureRevision = it.signatureRevision + 1,
                    successMessage = null,
                    errorMessage = null,
                )
            }
        }
    }

    fun saveProfile(drawnBitmap: Bitmap?) {
        val state = _uiState.value
        if (state.companyName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Company name is required") }
            return
        }
        if (state.address.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Address is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val signaturePath = resolveSignaturePath(state, drawnBitmap)
            runCatching {
                companyProfileRepository.upsertProfile(
                    CompanyProfile(
                        companyName = state.companyName.trim(),
                        address = state.address.trim(),
                        gstin = state.gstin.trim(),
                        pan = state.pan.trim(),
                        phone = state.phone.trim(),
                        email = state.email.trim(),
                        state = "",
                        salesType = state.salesType.trim(),
                        accountHolderName = state.accountHolderName.trim(),
                        accountNumber = state.accountNumber.trim(),
                        ifscCode = state.ifscCode.trim().uppercase(),
                        accountType = state.accountType.trim(),
                        bankName = state.bankName.trim(),
                        signaturePath = signaturePath,
                    ),
                )
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to save profile") }
            }
            _uiState.update {
                if (it.errorMessage == null) {
                    it.copy(
                        isSaving = false,
                        successMessage = "Profile saved",
                        signaturePath = signaturePath,
                        signatureCleared = false,
                        signatureRevision = it.signatureRevision + 1,
                    )
                } else {
                    it.copy(isSaving = false)
                }
            }
        }
    }

    private fun resolveSignaturePath(state: ProfileUiState, drawnBitmap: Bitmap?): String {
        return when {
            state.signatureCleared -> ""
            state.signatureInputMode == SignatureInputMode.DRAW && drawnBitmap != null -> {
                if (state.signaturePath.isNotBlank()) {
                    SignatureStorage.delete(state.signaturePath)
                }
                SignatureStorage.save(appContext, drawnBitmap)
            }
            state.signatureInputMode == SignatureInputMode.IMAGE -> state.signaturePath
            drawnBitmap != null -> {
                if (state.signaturePath.isNotBlank()) {
                    SignatureStorage.delete(state.signaturePath)
                }
                SignatureStorage.save(appContext, drawnBitmap)
            }
            else -> state.signaturePath
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
