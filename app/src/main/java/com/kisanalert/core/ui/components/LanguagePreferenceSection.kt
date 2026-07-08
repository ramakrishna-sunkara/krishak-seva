package com.kisanalert.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kisanalert.R
import com.kisanalert.core.ui.localization.localizedLabel
import com.kisanalert.domain.model.PreferredLanguage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagePreferenceSection(
    selectedLanguage: PreferredLanguage,
    languageOptions: List<PreferredLanguage>,
    onLanguageSelected: (PreferredLanguage) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    showHeader: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showHeader) {
            Text(
                text = stringResource(R.string.settings_language_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.settings_language_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languageOptions.forEach { language ->
                FilterChip(
                    selected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) },
                    enabled = isEnabled,
                    label = { Text(text = language.localizedLabel()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
