package com.example.data.local

import androidx.room.*
import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isPinned DESC, positionIndex ASC, createdAt DESC")
    fun getAllTasksWithSubTasks(): Flow<List<TaskWithSubTasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY isPinned DESC, positionIndex ASC, createdAt DESC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskWithSubTasks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask): Long

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: Long)

    @Query("UPDATE tasks SET positionIndex = :position WHERE id = :taskId")
    suspend fun updateTaskPosition(taskId: Long, position: Int)

    @Query("UPDATE tasks SET status = :status, positionIndex = :position WHERE id = :taskId")
    suspend fun updateTaskStatusAndPosition(taskId: Long, status: TaskStatus, position: Int)

    @Transaction
    suspend fun updateTasksOrder(taskPositions: List<Pair<Long, Int>>) {
        taskPositions.forEach { (taskId, position) ->
            updateTaskPosition(taskId, position)
        }
    }

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM subtasks")
    suspend fun deleteAllSubTasks()

    @Transaction
    suspend fun clearAllData() {
        deleteAllSubTasks()
        deleteAllTasks()
    }
}
