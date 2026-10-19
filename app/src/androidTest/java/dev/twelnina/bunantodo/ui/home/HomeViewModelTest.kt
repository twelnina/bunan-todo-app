package dev.twelnina.bunantodo.ui.home

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.twelnina.bunantodo.data.local.AppDatabase
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.model.Todo
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class HomeViewModelTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun uiState_updatesItemAndDaysSincePlannedDateWhenDateChanges() = runTest(timeout = 10.seconds) {
        val firstDate = LocalDate.of(2026, 9, 21)
        val nextDate = firstDate.plusDays(1)

        val earlierIncomplete = createTodo(1, firstDate.minusDays(1))
        val plannedToday = createTodo(2, firstDate)
        val plannedTomorrow = createTodo(3, nextDate)
        val completedEarlier = createTodo(4, firstDate.minusDays(1), true)

        val repository = TodoRepository(database.todoDao())
        listOf(earlierIncomplete, plannedToday, plannedTomorrow, completedEarlier).forEach {
            repository.insert(it)
        }

        val dateProvider = FakeCurrentDateProvider(firstDate)
        val viewModel = HomeViewModel(
            repository = repository, currentDateProvider = dateProvider
        )

        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.uiState.collect { }
        }

        try {
            val before = viewModel.uiState.first {
                it.today == firstDate
            }
            assertEquals(listOf(plannedToday), before.todayItems)
            assertEquals(
                listOf(earlierIncomplete),
                before.pastIncompleteItems
            )
            assertEquals(
                listOf(1L),
                before.pastIncompleteItems.map { it.calculateDaysSincePlannedDate(firstDate) }
            )

            dateProvider.changeDate(nextDate)

            val after = viewModel.uiState.first {
                it.today == nextDate
            }
            assertEquals(listOf(plannedTomorrow), after.todayItems)
            assertEquals(
                listOf(earlierIncomplete, plannedToday),
                after.pastIncompleteItems
            )
            assertEquals(
                listOf(2L, 1L),
                after.pastIncompleteItems.map { it.calculateDaysSincePlannedDate(nextDate) }
            )
        } finally {
            collector.cancelAndJoin()
            viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        }
    }

    private fun createTodo(id: Int, plannedDate: LocalDate, isCompleted: Boolean = false) =
        Todo(
            id = id,
            title = "Todo $id",
            detail = "",
            plannedDate = plannedDate,
            tag = null,
            isCompleted = isCompleted
        )
}
