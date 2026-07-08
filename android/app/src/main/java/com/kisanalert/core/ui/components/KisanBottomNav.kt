package com.kisanalert.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kisanalert.R
import com.kisanalert.core.constants.BottomNavRoutes
import com.kisanalert.core.ui.theme.KisanColors

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

val kisanBottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(
        route = BottomNavRoutes.HOME,
        labelRes = R.string.nav_home,
        icon = Icons.Rounded.Home
    ),
    BottomNavItem(
        route = BottomNavRoutes.CROPS,
        labelRes = R.string.nav_crops,
        icon = Icons.Rounded.Eco
    ),
    BottomNavItem(
        route = BottomNavRoutes.DOCTOR,
        labelRes = R.string.nav_doctor,
        icon = Icons.Rounded.Biotech
    ),
    BottomNavItem(
        route = BottomNavRoutes.WEATHER,
        labelRes = R.string.nav_weather,
        icon = Icons.Rounded.Cloud
    ),
    BottomNavItem(
        route = BottomNavRoutes.ACCOUNT,
        labelRes = R.string.nav_account,
        icon = Icons.Rounded.Person
    )
)

@Composable
fun KisanBottomNavBar(
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    accountBadgeCount: Int = 0
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = KisanColors.SectionDivider
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        kisanBottomNavItems.forEach { item ->
            val isSelected: Boolean = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.route) },
                icon = {
                    val iconContent: @Composable () -> Unit = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.labelRes)
                        )
                    }
                    if (item.route == BottomNavRoutes.ACCOUNT && accountBadgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(text = accountBadgeCount.toString())
                                }
                            }
                        ) {
                            iconContent()
                        }
                    } else {
                        iconContent()
                    }
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = KisanColors.AccentTint,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
