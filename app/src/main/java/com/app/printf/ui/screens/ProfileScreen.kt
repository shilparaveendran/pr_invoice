package com.app.printf.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.ui.components.PrintfExpandableSection
import com.app.printf.ui.components.PrintfOutlinedButton
import com.app.printf.ui.components.PrintfPrimaryButton
import com.app.printf.ui.components.PrintfScreenBackground
import com.app.printf.ui.components.PrintfSectionHeader
import com.app.printf.ui.components.PrintfTextField
import com.app.printf.ui.components.SalesTypeSelector
import com.app.printf.ui.components.SignatureModeSelector
import com.app.printf.ui.components.SignaturePad
import com.app.printf.ui.components.rememberSignaturePadController
import com.app.printf.ui.state.ProfileUiState
import com.app.printf.ui.state.SignatureInputMode
import com.app.printf.ui.viewmodel.ProfileViewModel
import com.app.printf.util.SignatureStorage
import com.app.printf.util.TaxConstants

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val signatureController = rememberSignaturePadController()
    val signatureImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.onSignatureImageUri(uri)
    }

    LaunchedEffect(uiState.signatureInputMode) {
        if (uiState.signatureInputMode == SignatureInputMode.IMAGE) {
            signatureController.clear()
        }
    }

    val savedSignaturePreview = remember(
        uiState.signaturePath,
        uiState.signatureCleared,
        uiState.signatureRevision,
    ) {
        if (uiState.signatureCleared || uiState.signaturePath.isBlank()) {
            null
        } else {
            SignatureStorage.load(uiState.signaturePath)?.asImageBitmap()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        viewModel.clearSuccessMessage()
    }

    PrintfScreenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PrintfSectionHeader(title = stringResource(R.string.profile))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                uiState.errorMessage?.let { err ->
                    Text(text = err, color = MaterialTheme.colorScheme.error)
                }

                PrintfExpandableSection(
                    title = stringResource(R.string.company_details),
                    subtitle = stringResource(R.string.company_details_hint),
                    expanded = uiState.companySectionExpanded,
                    onExpandedChange = viewModel::setCompanySectionExpanded,
                    summary = companySectionSummary(uiState),
                    highlighted = uiState.companyName.isNotBlank(),
                ) {
                    PrintfTextField(
                        value = uiState.companyName,
                        onValueChange = viewModel::onCompanyNameChange,
                        label = stringResource(R.string.company_name),
                    )
                    PrintfTextField(
                        value = uiState.address,
                        onValueChange = viewModel::onAddressChange,
                        label = stringResource(R.string.company_address),
                        singleLine = false,
                        minLines = 4,
                    )
                    PrintfTextField(
                        value = uiState.gstin,
                        onValueChange = viewModel::onGstinChange,
                        label = stringResource(R.string.gstin),
                    )
                    PrintfTextField(
                        value = uiState.pan,
                        onValueChange = viewModel::onPanChange,
                        label = stringResource(R.string.pan),
                    )
                    SalesTypeSelector(
                        selected = uiState.salesType.ifBlank { TaxConstants.STATE_SALE },
                        onSelected = viewModel::onSalesTypeChange,
                    )
                    PrintfTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = stringResource(R.string.phone),
                    )
                    PrintfTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = stringResource(R.string.email),
                    )
                }

                PrintfExpandableSection(
                    title = stringResource(R.string.bank_details),
                    subtitle = stringResource(R.string.bank_details_hint),
                    expanded = uiState.bankSectionExpanded,
                    onExpandedChange = viewModel::setBankSectionExpanded,
                    summary = bankSectionSummary(uiState),
                    highlighted = uiState.accountNumber.isNotBlank() || uiState.bankName.isNotBlank(),
                ) {
                    PrintfTextField(
                        value = uiState.accountHolderName,
                        onValueChange = viewModel::onAccountHolderNameChange,
                        label = stringResource(R.string.account_holder_name),
                    )
                    PrintfTextField(
                        value = uiState.accountNumber,
                        onValueChange = viewModel::onAccountNumberChange,
                        label = stringResource(R.string.account_number),
                    )
                    PrintfTextField(
                        value = uiState.ifscCode,
                        onValueChange = viewModel::onIfscCodeChange,
                        label = stringResource(R.string.ifsc_code),
                    )
                    PrintfTextField(
                        value = uiState.accountType,
                        onValueChange = viewModel::onAccountTypeChange,
                        label = stringResource(R.string.account_type),
                    )
                    PrintfTextField(
                        value = uiState.bankName,
                        onValueChange = viewModel::onBankNameChange,
                        label = stringResource(R.string.bank_name),
                    )
                }

                PrintfExpandableSection(
                    title = stringResource(R.string.digital_signature),
                    subtitle = stringResource(R.string.digital_signature_hint),
                    expanded = uiState.signatureSectionExpanded,
                    onExpandedChange = viewModel::setSignatureSectionExpanded,
                    summary = signatureSectionSummary(uiState),
                    highlighted = savedSignaturePreview != null,
                ) {
                    SignatureModeSelector(
                        selected = uiState.signatureInputMode,
                        onSelected = viewModel::setSignatureInputMode,
                    )

                    if (savedSignaturePreview != null) {
                        Text(
                            text = stringResource(R.string.saved_signature),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Image(
                            bitmap = savedSignaturePreview,
                            contentDescription = stringResource(R.string.saved_signature),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    when (uiState.signatureInputMode) {
                        SignatureInputMode.IMAGE -> {
                            PrintfOutlinedButton(
                                text = stringResource(R.string.upload_signature_image),
                                onClick = { signatureImagePicker.launch(arrayOf("image/*")) },
                            )
                        }
                        SignatureInputMode.DRAW -> {
                            Text(
                                text = stringResource(R.string.signature_pad_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            SignaturePad(controller = signatureController)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                signatureController.clear()
                                viewModel.markSignatureCleared()
                            },
                        ) {
                            Text(stringResource(R.string.clear_signature))
                        }
                    }
                }

                PrintfPrimaryButton(
                    text = stringResource(R.string.save_profile),
                    onClick = {
                        val drawnBitmap = if (
                            uiState.signatureInputMode == SignatureInputMode.DRAW &&
                            signatureController.hasContent()
                        ) {
                            signatureController.toBitmap()
                        } else {
                            null
                        }
                        viewModel.saveProfile(drawnBitmap)
                    },
                    enabled = !uiState.isSaving,
                )
                if (uiState.isSaving) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun companySectionSummary(uiState: ProfileUiState): String {
    return when {
        uiState.companyName.isNotBlank() -> uiState.companyName
        else -> stringResource(R.string.profile_section_not_set)
    }
}

@Composable
private fun bankSectionSummary(uiState: ProfileUiState): String {
    return when {
        uiState.bankName.isNotBlank() -> uiState.bankName
        uiState.accountHolderName.isNotBlank() -> uiState.accountHolderName
        uiState.accountNumber.isNotBlank() -> uiState.accountNumber
        else -> stringResource(R.string.profile_section_not_set)
    }
}

@Composable
private fun signatureSectionSummary(uiState: ProfileUiState): String {
    return when {
        !uiState.signatureCleared && uiState.signaturePath.isNotBlank() -> {
            when (uiState.signatureInputMode) {
                SignatureInputMode.IMAGE -> stringResource(R.string.profile_signature_saved)
                SignatureInputMode.DRAW -> stringResource(R.string.signature_mode_draw)
            }
        }
        uiState.signatureInputMode == SignatureInputMode.IMAGE ->
            stringResource(R.string.signature_mode_upload)
        else -> stringResource(R.string.profile_signature_not_set)
    }
}
