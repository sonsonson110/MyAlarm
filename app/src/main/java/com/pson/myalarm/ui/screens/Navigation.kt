package com.pson.myalarm.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var savedAlarmId by remember { mutableLongStateOf(-1) }

    NavHost(navController = navController, startDestination = Screens.AlarmListScreen.route) {
        composable(route = Screens.AlarmListScreen.route) {
            AlarmListScreen(
                modifier = modifier,
                onEditAlarm = { alarmId ->
                    navController.navigate(
                        Screens.AlarmEditScreen.createRoute(
                            alarmId
                        )
                    )
                },
                recentSavedAlarmId = savedAlarmId,
                resetRecentSavedAlarmId = { savedAlarmId = -1 }
            )
        }
        composable(
            route = Screens.AlarmEditScreen.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
        ) {
            AlarmEditScreen(onNavigateUp = { recentlySavedAlarmId ->
                // Avoid re-trigger old highlighted item
                savedAlarmId = recentlySavedAlarmId ?: -1
                navController.popBackStack()
            })
        }

        composable(route = Screens.TimerScreen.route) { TimerScreen(modifier) }

        composable(route = Screens.StopwatchScreen.route) { StopwatchScreen(modifier) }
    }
}