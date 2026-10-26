package dev.twelnina.bunantodo.ui.list

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.twelnina.bunantodo.data.local.AppDatabase
import dev.twelnina.bunantodo.data.local.TodoDao
import dev.twelnina.bunantodo.data.local.TodoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class TodoDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var todoDao: TodoDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        todoDao = database.todoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun observeTodayItems_returnsOnlyTodosPlannedToday() = runTest(timeout = 10.seconds) {
        val today = LocalDate.of(2026, 9, 19)

        val plannedToday = TodoEntity(
            id = 1,
            title = "Planned for today",
            detail = "Should be returned",
            plannedDate = today,
            tag = null
        )

        val plannedTomorrow = TodoEntity(
            id = 2,
            title = "Planned for tomorrow",
            detail = "Should not be returned",
            plannedDate = today.plusDays(1),
            tag = null
        )

        todoDao.insert(plannedToday)
        todoDao.insert(plannedTomorrow)

        val actual = todoDao.observeTodayItems(today).first()

        assertEquals(listOf(plannedToday), actual)
    }

    @Test
    fun observeOverdueItems_returnsOnlyIncompleteTodosBeforeToday() = runTest(timeout = 10.seconds) {
        val today = LocalDate.of(2026, 9, 19)

        val overdueTodo = TodoEntity(
            id = 1,
            title = "Overdue task",
            detail = "Should be returned",
            plannedDate = today.minusDays(1),
            tag = null
        )

        val plannedToday = TodoEntity(
            id = 2,
            title = "Planned for today",
            detail = "Should not be returned",
            plannedDate = today,
            tag = null
        )

        val completedEarlier = TodoEntity(
            id = 3,
            title = "Completed earlier task",
            detail = "Should not be returned",
            plannedDate = today.minusDays(2),
            tag = null,
            isCompleted = true
        )

        todoDao.insert(overdueTodo)
        todoDao.insert(plannedToday)
        todoDao.insert(completedEarlier)

        val actual = todoDao.observeOverdueItems(today).first()

        assertEquals(listOf(overdueTodo), actual)
    }
}
