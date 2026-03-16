package com.japygo.modakmodak.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.japygo.modakmodak.R
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.White

@Composable
fun ModakBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        modifier = Modifier.height(64.dp),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.ShoppingCart,
                    contentDescription = stringResource(R.string.nav_shop),
                )
            },
            selected = currentRoute == "shop",
            onClick = {
                if (currentRoute != "shop") {
                    navController.navigate("shop") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FireOrange,
                unselectedIconColor = White.copy(alpha = 0.5f),
                indicatorColor = FireOrange.copy(alpha = 0.1f),
            ),
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.LocalMall,
                    contentDescription = stringResource(R.string.nav_bag),
                )
            },
            selected = currentRoute == "bag",
            onClick = {
                if (currentRoute != "bag") {
                    navController.navigate("bag") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FireOrange,
                unselectedIconColor = White.copy(alpha = 0.5f),
                indicatorColor = FireOrange.copy(alpha = 0.1f),
            ),
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = stringResource(R.string.nav_home),
                )
            },
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FireOrange,
                unselectedIconColor = White.copy(alpha = 0.5f),
                indicatorColor = FireOrange.copy(alpha = 0.1f),
            ),
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.BarChart,
                    contentDescription = stringResource(R.string.nav_stats),
                )
            },
            selected = currentRoute == "stats",
            onClick = {
                if (currentRoute != "stats") {
                    navController.navigate("stats") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FireOrange,
                unselectedIconColor = White.copy(alpha = 0.5f),
                indicatorColor = FireOrange.copy(alpha = 0.1f),
            ),
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                )
            },
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FireOrange,
                unselectedIconColor = White.copy(alpha = 0.5f),
                indicatorColor = FireOrange.copy(alpha = 0.1f),
            ),
        )
    }
}
