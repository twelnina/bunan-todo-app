package dev.twelnina.bunantodo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.twelnina.bunantodo.model.TodoTag
import java.time.LocalDate

@Entity(tableName = "todo_items")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    @ColumnInfo(name = "description") val detail: String,
    val plannedDate: LocalDate?,
    val tag: TodoTag?,
    val isCompleted: Boolean = false
)
