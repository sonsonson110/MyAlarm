package com.pson.myalarm.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pson.myalarm.ui.screens.alarm_edit.AlarmEditScreen
import com.pson.myalarm.ui.screens.alarm_list.AlarmListScreen
import com.pson.myalarm.ui.screens.stopwatch.StopwatchScreen
import com.pson.myalarm.ui.screens.timer.TimerScreen

@Composable
internal fun Navigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screens.AlarmScreen.route) {
        composable(route = Screens.AlarmScreen.route) {
            AlarmListScreen(
                modifier = modifier,
                onEditAlarm = { alarmId ->
                    navController.navigate(
                        Screens.AlarmEditScreen.createRoute(
                            alarmId
                        )
                    )
                }
            )
        }
        composable(
            route = Screens.AlarmEditScreen.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1
                })
        ) {
            AlarmEditScreen(onNavigateUp = { navController.popBackStack() })
        }

        composable(route = Screens.TimerScreen.route) { TimerScreen(modifier) }

        composable(route = Screens.StopwatchScreen.route) { StopwatchScreen(modifier) }
    }
}