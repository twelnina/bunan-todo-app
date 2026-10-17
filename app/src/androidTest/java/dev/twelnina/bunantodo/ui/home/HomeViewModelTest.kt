package dev.twelnina.bunantodo.ui.home

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.twelnina.bunantodo.data.local.AppDatabase
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.data.repository.TodoRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

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
    fun uiState_updatesItemAndDaysSincePlannedDateWhenDateChanges() = runBlocking {
        val firstDate = LocalDate.of(2026, 9, 21)
        val nextDate = firstDate.plusDays(1)

        val earlierIncomplete = createTodo(1, firstDate.minusDays(1))
        val plannedToday = createTodo(2, firstDate)
        val plannedTomorrow = createTodo(3, nextDate)
        val completedEarlier = createTodo(4, firstDate.minusDays(1), true)

        val dao = database.todoDao()
        listOf(earlierIncomplete, plannedToday, plannedTomorrow, completedEarlier).forEach { dao.insert(it) }

        val dateProvider = FakeCurrentDateProvider(firstDate)
        val viewModel = HomeViewModel(
            repository = TodoRepository(dao), currentDateProvider = dateProvider
        )

        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.uiState.collect { }
        }

        try {
            withTimeout(5_000.milliseconds) {
                val before = viewModel.uiState.first {
                    it.today == firstDate
                }
                assertEquals(listOf(plannedToday), before.todayItems)
                assertEquals(
                    listOf(
                        PastIncompleteItem(todo = earlierIncomplete, daysSincePlannedDate = 1L)
                    ),
                    before.pastIncompleteItems
                )

                dateProvider.changeDate(nextDate)

                val after = viewModel.uiState.first {
                    it.today == nextDate
                }
                assertEquals(listOf(plannedTomorrow), after.todayItems)
                assertEquals(
                    listOf(
                        PastIncompleteItem(todo = earlierIncomplete, daysSincePlannedDate = 2L),
                        PastIncompleteItem(todo = plannedToday, daysSincePlannedDate = 1L)
                    ),
                    after.pastIncompleteItems
                )
            }
        } finally {
            collector.cancelAndJoin()
            viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        }
    }

    private fun createTodo(id: Int, plannedDate: LocalDate, isCompleted: Boolean = false) =
        TodoEntity(
            id = id,
            title = "Todo $id",
            detail = "",
            plannedDate = plannedDate,
            tag = null,
            isCompleted = isCompleted
        )
}