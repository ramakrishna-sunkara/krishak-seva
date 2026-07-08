package com.kisanalert.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kisanalert.R

@Composable
fun DataSourceBadge(
    isFromCloud: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor: Color
    val contentColor: Color
    val label: String
    val icon = if (isFromCloud) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
    if (isFromCloud) {
        backgroundColor = Color(0xFFE8F5E9)
        contentColor = Color(0xFF2E7D32)
        label = stringResource(R.string.data_source_online)
    } else {
        backgroundColor = Color(0xFFFFF8E1)
        contentColor = Color(0xFFF57F17)
        label = stringResource(R.string.data_source_offline)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
