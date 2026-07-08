package com.kisanalert.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kisanalert.R
import com.kisanalert.core.constants.FarmOptions
import com.kisanalert.domain.model.PostOfficeLocation

@Composable
fun LocationFormFields(
    pincode: String,
    village: String,
    district: String,
    state: String,
    stateOptions: List<String>,
    postOfficeOptions: List<PostOfficeLocation>,
    pincodeDistrictOptions: List<String>,
    isPincodeLoading: Boolean,
    pincodeLookupMessage: String?,
    onPincodeChanged: (String) -> Unit,
    onPostOfficeSelected: (PostOfficeLocation) -> Unit,
    onVillageChanged: (String) -> Unit,
    onDistrictChanged: (String) -> Unit,
    onStateChanged: (String) -> Unit
) {
    val districtOptions: List<String> = when {
        pincodeDistrictOptions.isNotEmpty() -> pincodeDistrictOptions
        state.isNotBlank() -> FarmOptions.STATE_DISTRICTS[state].orEmpty()
        else -> emptyList()
    }
    val selectedPostOffice: PostOfficeLocation? = postOfficeOptions.firstOrNull { office ->
        office.name == village && office.district == district && office.state == state
    }
    KisanTextField(
        value = pincode,
        onValueChange = onPincodeChanged,
        label = stringResource(R.string.registration_pincode_label),
        placeholder = stringResource(R.string.registration_pincode_placeholder),
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.LocationCity,
                contentDescription = null
            )
        }
    )
    if (isPincodeLoading) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = stringResource(R.string.registration_pincode_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (!pincodeLookupMessage.isNullOrBlank()) {
        Text(
            text = pincodeLookupMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    if (postOfficeOptions.isNotEmpty()) {
        KisanDropdownField(
            label = stringResource(R.string.registration_area_label),
            options = postOfficeOptions,
            selectedOption = selectedPostOffice,
            onOptionSelected = onPostOfficeSelected,
            optionLabel = { office -> office.name }
        )
    }
    KisanTextField(
        value = village,
        onValueChange = onVillageChanged,
        label = stringResource(R.string.registration_village_label),
        placeholder = stringResource(R.string.registration_village_placeholder),
        imeAction = ImeAction.Next,
        leadingContent = {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null
            )
        }
    )
    KisanDropdownField(
        label = stringResource(R.string.registration_state_label),
        options = stateOptions,
        selectedOption = state.takeIf { selectedState -> selectedState.isNotBlank() },
        onOptionSelected = onStateChanged,
        optionLabel = { selectedState -> selectedState }
    )
    if (districtOptions.isNotEmpty()) {
        KisanDropdownField(
            label = stringResource(R.string.registration_district_label),
            options = districtOptions,
            selectedOption = district.takeIf { selectedDistrict -> selectedDistrict.isNotBlank() },
            onOptionSelected = onDistrictChanged,
            optionLabel = { selectedDistrict -> selectedDistrict }
        )
    } else {
        KisanTextField(
            value = district,
            onValueChange = onDistrictChanged,
            label = stringResource(R.string.registration_district_label),
            placeholder = stringResource(R.string.registration_district_placeholder),
            imeAction = ImeAction.Next
        )
    }
}
