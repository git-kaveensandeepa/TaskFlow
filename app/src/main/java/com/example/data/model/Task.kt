package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}

enum class TaskRecurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val dueDateMillis: Long? = null,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val positionIndex: Int = 0
)
