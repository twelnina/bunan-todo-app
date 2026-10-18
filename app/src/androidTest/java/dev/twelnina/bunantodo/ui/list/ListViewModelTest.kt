package dev.twelnina.bunantodo.ui.list

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.twelnina.bunantodo.data.local.AppDatabase
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.PlannedDateFilter
import dev.twelnina.bunantodo.model.Todo
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class ListViewModelTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun uiState_updatesTodayFilteredTodosWhenDateChanges() =
        runTest(timeout = 10.seconds) {
            val firstDate = LocalDate.of(2026, 9, 24)
            val nextDate = firstDate.plusDays(1)

            val plannedToday = createTodo(1, firstDate)
            val plannedTomorrow = createTodo(2, nextDate)

            val repository = TodoRepository(database.todoDao())
            repository.insert(plannedToday)
            repository.insert(plannedTomorrow)

            val currentDate = MutableStateFlow(firstDate)
            val dateProvider = object : CurrentDateProvider {
                override fun observeDate(): Flow<LocalDate> = currentDate
            }

            val viewModel = ListViewModel(
                repository = repository,
                currentDateProvider = dateProvider,
                initialPlannedDateFilter = PlannedDateFilter.TODAY
            )

            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                viewModel.uiState.collect { }
            }

            try {
                val before = viewModel.uiState.first { state ->
                    state.today == firstDate
                }

                assertEquals(
                    listOf(plannedToday),
                    before.todoGroups.flatMap { group -> group.todos }
                )

                currentDate.value = nextDate

                val after = viewModel.uiState.first { state ->
                    state.today == nextDate
                }

                assertEquals(
                    listOf(plannedTomorrow),
                    after.todoGroups.flatMap { group -> group.todos }
                )
            } finally {
                collector.cancelAndJoin()
                viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
            }
        }

    private fun createTodo(id: Int, plannedDate: LocalDate): Todo {
        return Todo(
            id = id,
            title = "Todo $id",
            detail = "Description $id",
            plannedDate = plannedDate,
            tag = null
        )
    }
}