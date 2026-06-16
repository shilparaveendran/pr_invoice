package com.app.printf.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.app.printf.ui.theme.BrandTextDark
import com.app.printf.ui.theme.BrandWhite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.printf.R
import com.app.printf.util.TaxConstants

@Composable
fun SalesTypeSelector(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.sales_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val chipColors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandTextDark,
            selectedLabelColor = BrandWhite,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = BrandWhite,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !isInterstateSelection(selected),
                onClick = { onSelected(TaxConstants.STATE_SALE) },
                label = { Text(stringResource(R.string.state_sale)) },
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = !isInterstateSelection(selected),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    selectedBorderColor = BrandTextDark,
                ),
            )
            FilterChip(
                selected = isInterstateSelection(selected),
                onClick = { onSelected(TaxConstants.INTERSTATE_SALE) },
                label = { Text(stringResource(R.string.interstate_sale)) },
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isInterstateSelection(selected),
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    selectedBorderColor = BrandTextDark,
                ),
            )
        }
    }
}

private fun isInterstateSelection(selected: String): Boolean {
    return selected.equals(TaxConstants.INTERSTATE_SALE, ignoreCase = true) ||
        selected.contains("interstate", ignoreCase = true)
}
