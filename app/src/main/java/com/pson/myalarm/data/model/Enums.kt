package com.pson.myalarm.data.model

import java.util.Calendar

enum class DayOfWeek {

    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    val abbreviation: String
        get() = when (this) {
            MONDAY -> "M"
            TUESDAY -> "Tu"
            WEDNESDAY -> "W"
            THURSDAY -> "Th"
            FRIDAY -> "F"
            SATURDAY -> "Sa"
            SUNDAY -> "Su"
        }

    fun toCalendarDay(): Int {
        return when (this) {
            SUNDAY -> Calendar.SUNDAY
            MONDAY -> Calendar.MONDAY
            TUESDAY -> Calendar.TUESDAY
            WEDNESDAY -> Calendar.WEDNESDAY
            THURSDAY -> Calendar.THURSDAY
            FRIDAY -> Calendar.FRIDAY
            SATURDAY -> Calendar.SATURDAY
        }
    }
}