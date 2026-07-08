package com.kisanalert.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kisanalert.core.ui.theme.KrishakSevaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> KisanDropdownField(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedOption?.let { option -> optionLabel(option) }.orEmpty()
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { expanded ->
            if (enabled) {
                isExpanded = expanded
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Dropdown — Selected")
@Composable
private fun KisanDropdownFieldSelectedPreview() {
    KrishakSevaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            KisanDropdownField(
                label = "Language",
                options = listOf("English", "Telugu", "Hindi"),
                selectedOption = "Telugu",
                onOptionSelected = {},
                optionLabel = { it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Dropdown — Empty")
@Composable
private fun KisanDropdownFieldEmptyPreview() {
    KrishakSevaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            KisanDropdownField(
                label = "Crop Type",
                options = listOf("Rice", "Cotton", "Maize"),
                selectedOption = null,
                onOptionSelected = {},
                optionLabel = { it }
            )
        }
    }
}
