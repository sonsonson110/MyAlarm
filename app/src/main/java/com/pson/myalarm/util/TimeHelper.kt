package com.pson.myalarm.util

import java.util.Calendar

object TimeHelper {
    fun nowInMillis() = System.currentTimeMillis()

    fun isToday(timestampMillis: Long): Boolean {
        // Get today's date
        val today = Calendar.getInstance().apply {
            timeInMillis = nowInMillis()
        }

        // Get the date of the provided timestamp
        val providedDate = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
        }

        // Compare year, month, and day
        return today.get(Calendar.YEAR) == providedDate.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == providedDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == providedDate.get(Calendar.DAY_OF_MONTH)
    }
}