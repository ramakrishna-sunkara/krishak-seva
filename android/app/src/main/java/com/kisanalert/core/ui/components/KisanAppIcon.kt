package com.kisanalert.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kisanalert.R
import com.kisanalert.core.ui.theme.KisanAlertTheme

enum class KisanAppIconStyle {
    BULLOCK_CART,
    TRACTOR
}

object KisanAppIconResources {
    val BULLOCK_CART: Int = R.drawable.ic_bullock_cart
    val TRACTOR: Int = R.drawable.ic_tractor
}

@Composable
fun KisanAppIcon(
    modifier: Modifier = Modifier,
    style: KisanAppIconStyle = KisanAppIconStyle.BULLOCK_CART,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    val imageVector: ImageVector = when (style) {
        KisanAppIconStyle.BULLOCK_CART -> ImageVector.vectorResource(id = KisanAppIconResources.BULLOCK_CART)
        KisanAppIconStyle.TRACTOR -> ImageVector.vectorResource(id = KisanAppIconResources.TRACTOR)
    }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun KisanAppIconFromDrawable(
    @DrawableRes drawableId: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null
) {
    Icon(
        imageVector = ImageVector.vectorResource(id = drawableId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Preview(showBackground = true, name = "App Icons")
@Composable
private fun KisanAppIconPreview() {
    KisanAlertTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            KisanAppIcon(
                modifier = Modifier.size(48.dp),
                style = KisanAppIconStyle.BULLOCK_CART,
                tint = MaterialTheme.colorScheme.primary
            )
            KisanAppIcon(
                modifier = Modifier.size(48.dp),
                style = KisanAppIconStyle.TRACTOR,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, name = "Splash Size Icon")
@Composable
private fun KisanAppIconSplashPreview() {
    KisanAlertTheme {
        KisanAppIcon(
            modifier = Modifier
                .padding(16.dp)
                .size(72.dp),
            style = KisanAppIconStyle.BULLOCK_CART,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
