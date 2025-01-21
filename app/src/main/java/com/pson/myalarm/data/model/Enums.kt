package com.pson.myalarm.data.model

enum class DateOfWeek {

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
}