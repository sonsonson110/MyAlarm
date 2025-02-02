package com.pson.myalarm

import com.pson.myalarm.core.alarm.AlarmScheduler
import com.pson.myalarm.data.model.Alarm
import com.pson.myalarm.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.data.model.DayOfWeek
import com.pson.myalarm.data.model.WeeklySchedule
import com.pson.myalarm.util.TimeHelper
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalTime
import java.util.Calendar

class AlarmSchedulerTest {

    private lateinit var mockCurrentTime: Calendar
    private lateinit var baseAlarm: Alarm

    @Before
    fun setup() {
        // Set up a fixed current time for testing (Sunday 2025-02-02 10:00:00)
        mockCurrentTime = Calendar.getInstance().apply {
            set(2025, Calendar.FEBRUARY, 2, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Mock TimeHelper to return fixed time
        mockkObject(TimeHelper)
        every { TimeHelper.nowInMillis() } returns mockCurrentTime.timeInMillis

        // Basic alarm setup
        baseAlarm = Alarm(
            id = 1,
            alarmTime = LocalTime.of(11, 0),
            note = "Test alarm",
            isActive = true
        )
    }

    @Test
    fun `test one-time alarm (no repeat day) scheduled for future time on same day`() {
        // Arrange
        val alarm = AlarmWithWeeklySchedules(
            alarm = baseAlarm,
            weeklySchedules = emptyList()
        )
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert
        assertEquals(expected.timeInMillis, scheduledTime)
    }

    @Test
    fun `test one-time alarm (no repeat day) scheduled for next day when alarm time has passed`() {
        // Arrange
        val pastAlarm = baseAlarm.copy(alarmTime = LocalTime.of(9, 0))  // Earlier than current time
        val alarm = AlarmWithWeeklySchedules(
            alarm = pastAlarm,
            weeklySchedules = emptyList()
        )
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert

        assertEquals(expected.timeInMillis, scheduledTime)
    }

    @Test
    fun `test weekly alarm with single day - scheduled for same day and future time`() {
        // Arrange
        val alarm = AlarmWithWeeklySchedules(
            alarm = baseAlarm,
            weeklySchedules = listOf(
                WeeklySchedule(
                    id = 1,
                    alarmId = baseAlarm.id,
                    dayOfWeek = DayOfWeek.SUNDAY
                )
            )
        )
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert
        assertEquals(expected.timeInMillis, scheduledTime)
    }

    @Test
    fun `test weekly alarm with single day - jumps to next week when time passed`() {
        // Arrange
        val pastAlarm = baseAlarm.copy(alarmTime = LocalTime.of(9, 0))
        val alarm = AlarmWithWeeklySchedules(
            alarm = pastAlarm,
            weeklySchedules = listOf(
                WeeklySchedule(
                    id = 1,
                    alarmId = pastAlarm.id,
                    dayOfWeek = DayOfWeek.SUNDAY
                )
            )
        )

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 7)
        }
        assertEquals(expected.timeInMillis, scheduledTime)
    }

    @Test
    fun `test weekly alarm with multiple days - finds next available day`() {
        // Arrange
        val alarm = AlarmWithWeeklySchedules(
            alarm = baseAlarm,
            weeklySchedules = listOf(
                WeeklySchedule(
                    id = 1,
                    alarmId = baseAlarm.id,
                    dayOfWeek = DayOfWeek.WEDNESDAY
                ),
                WeeklySchedule(
                    id = 2,
                    alarmId = baseAlarm.id,
                    dayOfWeek = DayOfWeek.FRIDAY
                )
            )
        )

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Assuming current day is Sunday, should schedule for next Wednesday
            add(Calendar.DAY_OF_MONTH, 3)
        }
        assertEquals(expected.timeInMillis, scheduledTime)
    }

    @Test
    fun `test weekly alarm with multiple days - moves to next day when time passed`() {
        // Arrange
        val pastAlarm = baseAlarm.copy(alarmTime = LocalTime.of(9, 0))
        val alarm = AlarmWithWeeklySchedules(
            alarm = pastAlarm,
            weeklySchedules = listOf(
                WeeklySchedule(
                    id = 1,
                    alarmId = pastAlarm.id,
                    dayOfWeek = DayOfWeek.SUNDAY
                ),
                WeeklySchedule(
                    id = 2,
                    alarmId = pastAlarm.id,
                    dayOfWeek = DayOfWeek.WEDNESDAY
                ),
                WeeklySchedule(
                    id = 3,
                    alarmId = pastAlarm.id,
                    dayOfWeek = DayOfWeek.FRIDAY
                )
            )
        )

        // Act
        val scheduledTime = AlarmScheduler.getFutureScheduleTime(alarm)

        // Assert
        val expected = Calendar.getInstance().apply {
            timeInMillis = mockCurrentTime.timeInMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Should schedule for next Wednesday
            add(Calendar.DAY_OF_MONTH, 3)
        }
        assertEquals(expected.timeInMillis, scheduledTime)
    }
}