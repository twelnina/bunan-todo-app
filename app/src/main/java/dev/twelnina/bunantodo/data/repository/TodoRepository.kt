package dev.twelnina.bunantodo.data.repository

import dev.twelnina.bunantodo.data.local.TodoDao
import dev.twelnina.bunantodo.data.local.toEntity
import dev.twelnina.bunantodo.data.local.toTodo
import dev.twelnina.bunantodo.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TodoRepository(private val todoDao: TodoDao) {
    suspend fun insert(todo: Todo) {
        todoDao.insert(todo.toEntity())
    }

    suspend fun update(todo: Todo) {
        todoDao.update(todo.toEntity())
    }

    suspend fun delete(todo: Todo) {
        todoDao.delete(todo.toEntity())
    }

    suspend fun getItem(id: Int): Todo? {
        return todoDao.getItem(id)?.toTodo()
    }

    fun observeAllItems(): Flow<List<Todo>> {
        return todoDao.observeAllItem().map { entities ->
            entities.map { it.toTodo() }
        }
    }

    fun observeTodayItems(today: LocalDate): Flow<List<Todo>> {
        return todoDao.observeTodayItems(today).map { entities ->
            entities.map { it.toTodo() }
        }
    }

    fun observeOverdueItems(today: LocalDate): Flow<List<Todo>> {
        return todoDao.observeOverdueItems(today).map { entities ->
            entities.map { it.toTodo() }
        }
    }
}
