package com.example.data.backup

import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskRecurrence
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun exportToJson(allTasks: List<TaskWithSubTasks>): String {
        val rootJson = JSONObject()
        rootJson.put("app", "TaskFlow")
        rootJson.put("version", 1)
        rootJson.put("exportedAt", System.currentTimeMillis())

        val tasksArray = JSONArray()
        for (item in allTasks) {
            val task = item.task
            val taskJson = JSONObject()
            taskJson.put("id", task.id)
            taskJson.put("title", task.title)
            taskJson.put("description", task.description)
            taskJson.put("category", task.category)
            taskJson.put("priority", task.priority.name)
            taskJson.put("status", task.status.name)
            taskJson.put("recurrence", task.recurrence.name)
            if (task.dueDateMillis != null) {
                taskJson.put("dueDateMillis", task.dueDateMillis)
            } else {
                taskJson.put("dueDateMillis", JSONObject.NULL)
            }
            taskJson.put("isPinned", task.isPinned)
            taskJson.put("createdAt", task.createdAt)
            taskJson.put("positionIndex", task.positionIndex)

            val subTasksArray = JSONArray()
            for (sub in item.subTasks) {
                val subJson = JSONObject()
                subJson.put("id", sub.id)
                subJson.put("title", sub.title)
                subJson.put("isCompleted", sub.isCompleted)
                subTasksArray.put(subJson)
            }
            taskJson.put("subTasks", subTasksArray)
            tasksArray.put(taskJson)
        }

        rootJson.put("tasks", tasksArray)
        return rootJson.toString(2)
    }

    fun parseFromJson(jsonString: String): Result<List<TaskWithSubTasks>> {
        return runCatching {
            val rootJson = JSONObject(jsonString)
            if (!rootJson.has("tasks")) {
                throw IllegalArgumentException("Invalid backup format: Missing 'tasks' array")
            }

            val tasksArray = rootJson.getJSONArray("tasks")
            val parsedList = mutableListOf<TaskWithSubTasks>()

            for (i in 0 until tasksArray.length()) {
                val taskJson = tasksArray.getJSONObject(i)
                val title = taskJson.optString("title", "").takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Task at index $i is missing title")

                val description = taskJson.optString("description", "")
                val category = taskJson.optString("category", "General")
                
                val priorityStr = taskJson.optString("priority", "MEDIUM")
                val priority = runCatching { TaskPriority.valueOf(priorityStr) }.getOrDefault(TaskPriority.MEDIUM)

                val statusStr = taskJson.optString("status", "TODO")
                val status = runCatching { TaskStatus.valueOf(statusStr) }.getOrDefault(TaskStatus.TODO)

                val recurrenceStr = taskJson.optString("recurrence", "NONE")
                val recurrence = runCatching { TaskRecurrence.valueOf(recurrenceStr) }.getOrDefault(TaskRecurrence.NONE)

                val dueDateMillis = if (taskJson.has("dueDateMillis") && !taskJson.isNull("dueDateMillis")) {
                    taskJson.getLong("dueDateMillis")
                } else null

                val isPinned = taskJson.optBoolean("isPinned", false)
                val createdAt = taskJson.optLong("createdAt", System.currentTimeMillis())
                val positionIndex = taskJson.optInt("positionIndex", i)

                val task = Task(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    status = status,
                    recurrence = recurrence,
                    dueDateMillis = dueDateMillis,
                    isPinned = isPinned,
                    createdAt = createdAt,
                    positionIndex = positionIndex
                )

                val subTasks = mutableListOf<SubTask>()
                if (taskJson.has("subTasks") && !taskJson.isNull("subTasks")) {
                    val subTasksArray = taskJson.getJSONArray("subTasks")
                    for (j in 0 until subTasksArray.length()) {
                        val subJson = subTasksArray.getJSONObject(j)
                        val subTitle = subJson.optString("title", "").trim()
                        if (subTitle.isNotEmpty()) {
                            val isCompleted = subJson.optBoolean("isCompleted", false)
                            subTasks.add(SubTask(taskId = 0, title = subTitle, isCompleted = isCompleted))
                        }
                    }
                }

                parsedList.add(TaskWithSubTasks(task = task, subTasks = subTasks))
            }

            parsedList
        }
    }
}
