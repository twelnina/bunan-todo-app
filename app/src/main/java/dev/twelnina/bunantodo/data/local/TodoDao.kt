package dev.twelnina.bunantodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todoEntity: TodoEntity)

    @Update
    suspend fun update(todoEntity: TodoEntity)

    @Delete
    suspend fun delete(todoEntity: TodoEntity)

    @Query(
        """
      SELECT * FROM todo_items
      WHERE plannedDate = :today
      ORDER BY id ASC
  """
    )
    fun observeTodayItems(today: LocalDate): Flow<List<TodoEntity>>

    @Query(
        """
      SELECT * FROM todo_items
      WHERE plannedDate < :today
        AND isCompleted = 0
      ORDER BY plannedDate ASC, id ASC
  """
    )
    fun observeOverdueItems(today: LocalDate): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo_items ORDER BY plannedDate IS NULL ASC, plannedDate ASC, id ASC")
    fun observeAllItem(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    suspend fun getItem(id: Int): TodoEntity?
}
