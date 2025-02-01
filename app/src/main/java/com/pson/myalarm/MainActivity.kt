package com.pson.myalarm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pson.myalarm.ui.screens.Navigation
import com.pson.myalarm.ui.screens.Screens
import com.pson.myalarm.ui.theme.MyAlarmTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAlarmTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        // Only show bottom nav when we're on main screens
                        val currentRoute =
                            navController.currentBackStackEntryAsState().value?.destination?.route
                        if (currentRoute in listOf(
                                Screens.AlarmListScreen.route,
                                Screens.TimerScreen.route,
                                Screens.StopwatchScreen.route
                            )
                        ) {
                            BottomNavigationBar(navController = navController)
                        }
                    },

                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Navigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
internal fun BottomNavigationBar(navController: NavHostController) {
    val topLevelRoutes = listOf(
        TopLevelRoute(
            "Alarm",
            Screens.AlarmListScreen.route,
            ImageVector.vectorResource(R.drawable.outline_access_alarm_24)
        ),
        TopLevelRoute(
            "Stopwatch",
            Screens.StopwatchScreen.route,
            ImageVector.vectorResource(R.drawable.outline_timer_24)
        ),
        TopLevelRoute(
            "Timer",
            Screens.TimerScreen.route,
            ImageVector.vectorResource(R.drawable.outline_hourglass_24)
        )
    )
    NavigationBar() {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        topLevelRoutes.forEach { topLevelRoute ->
            NavigationBarItem(
                icon = { Icon(topLevelRoute.icon, contentDescription = topLevelRoute.name) },
                label = { Text(topLevelRoute.name) },
                selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(
                        topLevelRoute.route,
                        null
                    )
                } == true,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class TopLevelRoute(val name: String, val route: String, val icon: ImageVector)

