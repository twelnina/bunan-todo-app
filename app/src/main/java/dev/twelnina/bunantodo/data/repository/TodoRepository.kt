package dev.twelnina.bunantodo.data.repository

import dev.twelnina.bunantodo.data.local.TodoDao
import dev.twelnina.bunantodo.data.local.TodoEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TodoRepository(private val todoDao: TodoDao) {
    suspend fun insert(todoEntity: TodoEntity) {
        todoDao.insert(todoEntity)
    }

    suspend fun update(todoEntity: TodoEntity) {
        todoDao.update(todoEntity)
    }

    suspend fun delete(todoEntity: TodoEntity) {
        todoDao.delete(todoEntity)
    }

    fun observeAllItems(): Flow<List<TodoEntity>> {
        return todoDao.observeAllItem()
    }

    suspend fun getItem(id: Int): TodoEntity? {
        return todoDao.getItem(id)
    }

    fun observeTodayItems(today: LocalDate): Flow<List<TodoEntity>> {
        return todoDao.observeTodayItems(today)
    }

    fun observePastIncompleteItems(today: LocalDate): Flow<List<TodoEntity>> {
        return todoDao.observePastIncompleteItems(today)
    }
}