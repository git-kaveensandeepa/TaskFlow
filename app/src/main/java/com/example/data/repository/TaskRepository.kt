package com.example.data.repository

import com.example.data.backup.BackupManager
import com.example.data.local.TaskDao
import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskWithSubTasks>> = taskDao.getAllTasksWithSubTasks()

    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskWithSubTasks>> {
        return taskDao.getTasksByStatus(status)
    }

    suspend fun insertTaskWithSubTasks(task: Task, subTasks: List<SubTask>): Long {
        val taskId = taskDao.insertTask(task)
        subTasks.forEach { subTask ->
            taskDao.insertSubTask(subTask.copy(taskId = taskId))
        }
        return taskId
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun updateTaskWithSubTasks(task: Task, subTasks: List<SubTask>) {
        taskDao.updateTask(task)
        taskDao.deleteSubTasksForTask(task.id)
        subTasks.forEach { subTask ->
            taskDao.insertSubTask(subTask.copy(taskId = task.id))
        }
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompleted(taskWithSubTasks: TaskWithSubTasks) {
        val currentTask = taskWithSubTasks.task
        val newStatus = if (currentTask.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
        taskDao.updateTask(currentTask.copy(status = newStatus))
    }

    suspend fun updateSubTask(subTask: SubTask) {
        taskDao.updateSubTask(subTask)
    }

    suspend fun updateTaskStatusAndPosition(taskId: Long, status: TaskStatus, newPosition: Int) {
        taskDao.updateTaskStatusAndPosition(taskId, status, newPosition)
    }

    suspend fun reorderTasks(taskPositions: List<Pair<Long, Int>>) {
        taskDao.updateTasksOrder(taskPositions)
    }

    suspend fun exportBackupJson(currentTasks: List<TaskWithSubTasks>): String {
        return BackupManager.exportToJson(currentTasks)
    }

    suspend fun importBackupJson(jsonString: String, replaceExisting: Boolean): Result<Int> {
        val parseResult = BackupManager.parseFromJson(jsonString)
        if (parseResult.isFailure) {
            return Result.failure(parseResult.exceptionOrNull() ?: Exception("Failed to parse JSON"))
        }

        val importedTasks = parseResult.getOrNull() ?: emptyList()
        if (replaceExisting) {
            taskDao.clearAllData()
        }

        var count = 0
        for (item in importedTasks) {
            val insertedId = taskDao.insertTask(item.task.copy(id = 0)) // Assign new auto-id
            for (sub in item.subTasks) {
                taskDao.insertSubTask(sub.copy(id = 0, taskId = insertedId))
            }
            count++
        }

        return Result.success(count)
    }

    suspend fun clearAllData() {
        taskDao.clearAllData()
    }
}
