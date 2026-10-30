package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.TaskPriority
import com.example.data.model.TaskRecurrence
import com.example.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = runCatching {
        TaskPriority.valueOf(value)
    }.getOrDefault(TaskPriority.MEDIUM)

    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = runCatching {
        TaskStatus.valueOf(value)
    }.getOrDefault(TaskStatus.TODO)

    @TypeConverter
    fun fromRecurrence(recurrence: TaskRecurrence): String = recurrence.name

    @TypeConverter
    fun toRecurrence(value: String): TaskRecurrence = runCatching {
        TaskRecurrence.valueOf(value)
    }.getOrDefault(TaskRecurrence.NONE)
}
