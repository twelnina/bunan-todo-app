package dev.twelnina.bunantodo.ui.edit

import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.twelnina.bunantodo.data.local.AppDatabase
import dev.twelnina.bunantodo.data.local.TodoEntity
import dev.twelnina.bunantodo.data.repository.TodoRepository
import dev.twelnina.bunantodo.data.time.CurrentDateProvider
import dev.twelnina.bunantodo.model.TodoTag
import dev.twelnina.bunantodo.ui.TodoAppViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

class EditViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TodoRepository
    private lateinit var editViewModel: EditViewModel
    private lateinit var appViewModel: TodoAppViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        repository = TodoRepository(database.todoDao())

        val dateProvider = object : CurrentDateProvider {
            override fun observeDate(): Flow<LocalDate> =
                flowOf(LocalDate.of(2026, 9, 23))
        }

        editViewModel = EditViewModel(
            todoRepository = repository,
            currentDateProvider = dateProvider
        )
        appViewModel = TodoAppViewModel(repository)
    }

    @After
    fun tearDown() = runTest(timeout = 10.seconds) {
        editViewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        appViewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        database.close()
    }

    @Test
    fun deleteAndRestore_completedTodo_preservesAllFields() {
        assertDeleteAndRestorePreservesAllFields(true)
    }

    @Test
    fun deleteAndRestore_incompleteTodo_preservesAllFields() {
        assertDeleteAndRestorePreservesAllFields(false)
    }

    private fun assertDeleteAndRestorePreservesAllFields(isCompleted: Boolean) =
        runTest(timeout = 10.seconds) {
            val originalTodo = TodoEntity(
                id = 1,
                title = "Study Kotlin",
                detail = "Review coroutine basics",
                plannedDate = LocalDate.of(2026, 9, 21),
                tag = TodoTag.STUDY,
                isCompleted = isCompleted
            )
            repository.insert(originalTodo)

            editViewModel.loadItem(originalTodo.id)
            editViewModel.uiState.first { it.id == originalTodo.id }

            val deletedTodoResult = CompletableDeferred<TodoEntity>()
            editViewModel.deleteTodo { deletedTodo ->
                deletedTodoResult.complete(deletedTodo)
            }
            val deletedTodo = deletedTodoResult.await()

            assertNull(repository.getItem(originalTodo.id))

            appViewModel.restoreDeletedTodo(deletedTodo)

            val restoredTodo = repository.getAllItems().first { todos ->
                todos.any { it.id == originalTodo.id }
            }.single { it.id == originalTodo.id }

            assertEquals(originalTodo, restoredTodo)
        }
}
