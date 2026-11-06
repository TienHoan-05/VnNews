package com.example.vnnews.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vnnews.ui.bookmarks.BookmarksScreen
import com.example.vnnews.ui.detail.DetailScreen
import com.example.vnnews.ui.home.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onToggleTheme: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val density = LocalDensity.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute in listOf(
        NewsDestination.Home.route,
        NewsDestination.Bookmarks.route
    )

    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    expandedHeight = maxOf(64.dp, with(density) { (if (fontScale >= 1.3f) 33.sp else 51.sp).toDp() } + 16.dp),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (currentRoute == NewsDestination.Home.route) "vnnews" else "Thư viện",
                                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp, lineHeight = 33.sp, letterSpacing = (-1).sp)
                                Text(".", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                            if (density.fontScale < 1.3f) Text(if (currentRoute == NewsDestination.Home.route) "MỘT NƠI · NHIỀU GÓC NHÌN" else "NHỮNG ĐIỀU ĐÁNG ĐỌC LẠI",
                                style = MaterialTheme.typography.labelSmall, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.padding(end = 16.dp)) {
                        IconButton(onClick = onToggleTheme, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Chuyển sang chế độ sáng" else "Chuyển sang chế độ tối",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(IntrinsicSize.Min).heightIn(min = 64.dp)
                            .selectableGroup().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            listOf(NewsDestination.Home.route, NewsDestination.Bookmarks.route).forEach { route ->
                                val selected = currentRoute == route
                                Row(Modifier.weight(1f).fillMaxHeight().selectable(selected = selected, role = Role.Tab, onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    Icon(if (route == NewsDestination.Home.route) Icons.Default.Home else Icons.Default.Bookmarks,
                                        null, Modifier.size(20.dp), tint = tint)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (route == NewsDestination.Home.route) "Bản tin" else "Đã lưu",
                                        style = MaterialTheme.typography.labelLarge, color = tint,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NewsDestination.Home.route,
            modifier = if (showBars) Modifier.padding(innerPadding) else Modifier.fillMaxSize()
        ) {
            composable(NewsDestination.Home.route) {
                HomeScreen(
                    onArticleClick = { url, title ->
                        navController.navigate(NewsDestination.Detail.createRoute(url, title))
                    }
                )
            }

            composable(NewsDestination.Bookmarks.route) {
                BookmarksScreen(
                    onBrowseNews = {
                        navController.navigate(NewsDestination.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onArticleClick = { url, title ->
                        navController.navigate(NewsDestination.Detail.createRoute(url, title))
                    }
                )
            }

            composable(
                route = NewsDestination.Detail.route,
                arguments = listOf(
                    navArgument("encodedUrl") { type = NavType.StringType },
                    navArgument("encodedTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl")
                val encodedTitle = backStackEntry.arguments?.getString("encodedTitle")
                val url = NewsDestination.Detail.decodeUrl(encodedUrl)
                val title = NewsDestination.Detail.decodeTitle(encodedTitle)

                DetailScreen(
                    url = url,
                    title = title,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
