package com.app.printf.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.ui.state.SignatureInputMode
import com.app.printf.ui.theme.BrandTextDark
import com.app.printf.ui.theme.BrandWhite

@Composable
fun SignatureModeSelector(
    selected: SignatureInputMode,
    onSelected: (SignatureInputMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = BrandTextDark,
        selectedLabelColor = BrandWhite,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurface,
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.signature_input_mode),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selected == SignatureInputMode.DRAW,
                onClick = { onSelected(SignatureInputMode.DRAW) },
                label = { Text(stringResource(R.string.signature_mode_draw)) },
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == SignatureInputMode.DRAW,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    selectedBorderColor = BrandTextDark,
                ),
            )
            FilterChip(
                selected = selected == SignatureInputMode.IMAGE,
                onClick = { onSelected(SignatureInputMode.IMAGE) },
                label = { Text(stringResource(R.string.signature_mode_upload)) },
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == SignatureInputMode.IMAGE,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    selectedBorderColor = BrandTextDark,
                ),
            )
        }
    }
}
