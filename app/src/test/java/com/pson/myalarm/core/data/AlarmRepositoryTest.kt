package com.pson.myalarm.core.data

import com.pson.myalarm.core.data.repository.AlarmRepository
import com.pson.myalarm.core.data.repository.IAlarmRepository
import com.pson.myalarm.core.data.dao.AlarmDao
import com.pson.myalarm.core.data.model.Alarm
import com.pson.myalarm.core.data.model.AlarmWithWeeklySchedules
import com.pson.myalarm.core.data.model.DayOfWeek
import com.pson.myalarm.core.data.model.WeeklySchedule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

class AlarmRepositoryTest {

    private lateinit var alarmDao: AlarmDao
    private lateinit var repository: IAlarmRepository

    @Before
    fun setup() {
        alarmDao = mockk()
        repository = AlarmRepository(alarmDao)
    }

    @Test
    fun `saveAlarm delegates to dao and returns id`() = runTest {
        // Given
        val testId = 1L
        val testAlarm = createTestAlarm()
        coEvery { alarmDao.saveAlarm(testAlarm) } returns testId

        // When
        val result = repository.saveAlarm(testAlarm)

        // Then
        assertEquals(testId, result)
        coVerify(exactly = 1) { alarmDao.saveAlarm(testAlarm) }
    }

    @Test
    fun `deleteAlarm delegates to dao`() = runTest {
        // Given
        val alarmId = 1L
        coEvery { alarmDao.deleteAlarm(alarmId) } returns Unit

        // When
        repository.deleteAlarm(alarmId)

        // Then
        coVerify(exactly = 1) { alarmDao.deleteAlarm(alarmId) }
    }

    @Test
    fun `deleteAlarms delegates to dao with correct list`() = runTest {
        // Given
        val alarmIds = listOf(1L, 2L, 3L)
        coEvery { alarmDao.deleteAlarms(alarmIds) } returns Unit

        // When
        repository.deleteAlarms(alarmIds)

        // Then
        coVerify(exactly = 1) { alarmDao.deleteAlarms(alarmIds) }
    }

    @Test
    fun `toggleAlarmActivation delegates to dao`() = runTest {
        // Given
        val alarmId = 1L
        coEvery { alarmDao.toggleAlarmActivation(alarmId) } returns Unit

        // When
        repository.toggleAlarmActivation(alarmId)

        // Then
        coVerify(exactly = 1) { alarmDao.toggleAlarmActivation(alarmId) }
    }

    @Test
    fun `getAlarm returns alarm from dao`() = runTest {
        // Given
        val alarmId = 1L
        val expectedAlarm = createTestAlarm()
        coEvery { alarmDao.getById(alarmId) } returns expectedAlarm

        // When
        val result = repository.getAlarm(alarmId)

        // Then
        assertEquals(expectedAlarm, result)
        coVerify(exactly = 1) { alarmDao.getById(alarmId) }
    }

    @Test
    fun `getAlarm returns null when alarm not found`() = runTest {
        // Given
        val alarmId = 1L
        coEvery { alarmDao.getById(alarmId) } returns null

        // When
        val result = repository.getAlarm(alarmId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { alarmDao.getById(alarmId) }
    }

    @Test
    fun `getAllAlarms returns flow from dao`() = runTest {
        // Given
        val testAlarms = listOf(createTestAlarm(), createTestAlarm())
        coEvery { alarmDao.getAll() } returns flowOf(testAlarms)

        // When
        val result = repository.getAllAlarms().first()

        // Then
        assertEquals(testAlarms, result)
        coVerify(exactly = 1) { alarmDao.getAll() }
    }

    @Test
    fun `getAllAlarms returns empty list when no alarms exist`() = runTest {
        // Given
        coEvery { alarmDao.getAll() } returns flowOf(emptyList())

        // When
        val result = repository.getAllAlarms().first()

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { alarmDao.getAll() }
    }

    private fun createTestAlarm() = AlarmWithWeeklySchedules(
        alarm = Alarm(
            id = 0L,
            alarmTime = LocalTime.of(3, 0, 0),
            note = "Test alarm",
            snoozeTimeMinutes = 15,
            isActive = true
        ),
        weeklySchedules = listOf(
            WeeklySchedule(
                id = 0L,
                alarmId = 0L,
                dayOfWeek = DayOfWeek.MONDAY,
            ),
            WeeklySchedule(
                id = 1L,
                alarmId = 0L,
                dayOfWeek = DayOfWeek.THURSDAY,
            )
        ),
    )
}