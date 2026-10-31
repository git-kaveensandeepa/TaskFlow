package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryDao
import com.example.data.local.TaskDatabase
import com.example.data.model.Category
import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskRecurrence
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import com.example.data.repository.TaskRepository
import com.example.notification.NotificationHelper
import com.example.ui.components.CategoryUtils
import com.example.widget.TaskFlowWidgetProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val categoryDao: CategoryDao

    val allTasks: StateFlow<List<TaskWithSubTasks>>
    val customCategories: StateFlow<List<Category>>

    // UI Controls
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")
    val selectedPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val isDarkMode = MutableStateFlow(false) // Light theme default as requested!
    val activeTab = MutableStateFlow(0) // 0 = Board, 1 = List, 2 = Focus, 3 = Analytics, 4 = Settings

    // Multi-Select & Bulk Actions
    val selectedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val isMultiSelectMode: StateFlow<Boolean>

    val filteredTasks: StateFlow<List<TaskWithSubTasks>>

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        categoryDao = database.categoryDao()

        // Create notification channel on init
        NotificationHelper.createNotificationChannel(application)

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        customCategories = categoryDao.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        isMultiSelectMode = selectedTaskIds.map { it.isNotEmpty() }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

        // Sync custom categories with CategoryUtils
        viewModelScope.launch {
            customCategories.collect { list ->
                CategoryUtils.registerCustomCategories(list)
            }
        }

        // Trigger home screen widget updates whenever tasks change
        viewModelScope.launch {
            allTasks.collect {
                TaskFlowWidgetProvider.sendUpdateBroadcast(application)
            }
        }

        filteredTasks = combine(
            allTasks,
            searchQuery,
            selectedCategoryFilter,
            selectedPriorityFilter
        ) { tasks, query, category, priority ->
            tasks.filter { item ->
                val task = item.task
                val matchesQuery = query.isBlank() ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)

                val matchesCategory = category == "All" || task.category.equals(category, ignoreCase = true)
                val matchesPriority = priority == null || task.priority == priority

                matchesQuery && matchesCategory && matchesPriority
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial example tasks only once on first run and clean up any existing duplicates
        val prefs = application.getSharedPreferences("taskflow_prefs", android.content.Context.MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_sample_tasks_v2", false)

        viewModelScope.launch {
            if (!hasSeeded) {
                val current = repository.allTasks.first()
                if (current.isEmpty()) {
                    seedSampleData()
                }
                prefs.edit().putBoolean("has_seeded_sample_tasks_v2", true).apply()
            }
            cleanupDuplicateTasks()
        }
    }

    private suspend fun cleanupDuplicateTasks() {
        val currentTasks = repository.allTasks.first()
        val seenSignatures = mutableSetOf<String>()
        currentTasks.forEach { item ->
            val sig = "${item.task.title.trim()}_${item.task.category.trim()}_${item.task.recurrence.name}"
            if (seenSignatures.contains(sig)) {
                NotificationHelper.cancelTaskReminder(getApplication(), item.task.id)
                repository.deleteTask(item.task)
            } else {
                seenSignatures.add(sig)
            }
        }
    }

    private suspend fun seedSampleData() {
        val sample1 = Task(
            title = "Design TaskFlow iOS Glassmorphism UI",
            description = "Craft sleek translucent cards with rounded corners and light mode gradients.",
            category = "Work",
            priority = TaskPriority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            isPinned = true,
            positionIndex = 0
        )
        val subs1 = listOf(
            SubTask(taskId = 0, title = "Color palette & typography", isCompleted = true),
            SubTask(taskId = 0, title = "Drag and drop board columns", isCompleted = false)
        )
        repository.insertTaskWithSubTasks(sample1, subs1)

        val sample2 = Task(
            title = "Daily Gym Workout",
            description = "30 mins cardio & core training session.",
            category = "Health",
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO,
            recurrence = TaskRecurrence.DAILY,
            positionIndex = 1
        )
        repository.insertTaskWithSubTasks(sample2, emptyList())

        val sample3 = Task(
            title = "Grocery Shopping",
            description = "Buy fresh fruits, coffee beans, and green tea.",
            category = "Shopping",
            priority = TaskPriority.LOW,
            status = TaskStatus.DONE,
            positionIndex = 2
        )
        repository.insertTaskWithSubTasks(sample3, emptyList())
    }

    // Multi-Select Methods
    fun toggleTaskSelection(taskId: Long) {
        val current = selectedTaskIds.value.toMutableSet()
        if (current.contains(taskId)) {
            current.remove(taskId)
        } else {
            current.add(taskId)
        }
        selectedTaskIds.value = current
    }

    fun selectAllTasks(taskIds: List<Long>) {
        selectedTaskIds.value = taskIds.toSet()
    }

    fun clearTaskSelection() {
        selectedTaskIds.value = emptySet()
    }

    fun bulkDeleteSelected() {
        val ids = selectedTaskIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { taskId ->
                val taskWithSub = allTasks.value.find { it.task.id == taskId }
                if (taskWithSub != null) {
                    NotificationHelper.cancelTaskReminder(getApplication(), taskId)
                    repository.deleteTask(taskWithSub.task)
                }
            }
            clearTaskSelection()
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun bulkMarkStatus(newStatus: TaskStatus) {
        val ids = selectedTaskIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { taskId ->
                val taskWithSub = allTasks.value.find { it.task.id == taskId }
                if (taskWithSub != null) {
                    moveTaskStatus(taskWithSub, newStatus)
                }
            }
            clearTaskSelection()
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun bulkTogglePin(isPinned: Boolean) {
        val ids = selectedTaskIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { taskId ->
                val taskWithSub = allTasks.value.find { it.task.id == taskId }
                if (taskWithSub != null) {
                    repository.updateTask(taskWithSub.task.copy(isPinned = isPinned))
                }
            }
            clearTaskSelection()
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun addCustomCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            categoryDao.insertCategory(Category(name = name, colorHex = colorHex, iconName = iconName))
        }
    }

    fun deleteCustomCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.deleteCategory(category)
        }
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        selectedCategoryFilter.value = category
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        selectedPriorityFilter.value = if (selectedPriorityFilter.value == priority) null else priority
    }

    fun setActiveTab(tab: Int) {
        activeTab.value = tab
    }

    fun saveTask(task: Task, subTasks: List<SubTask>) {
        viewModelScope.launch {
            val savedTaskId: Long
            if (task.id == 0L) {
                savedTaskId = repository.insertTaskWithSubTasks(task, subTasks)
            } else {
                repository.updateTaskWithSubTasks(task, subTasks)
                savedTaskId = task.id
            }

            val savedTask = task.copy(id = savedTaskId)
            if (savedTask.status != TaskStatus.DONE && savedTask.dueDateMillis != null) {
                NotificationHelper.scheduleTaskReminder(getApplication(), savedTask)
            } else {
                NotificationHelper.cancelTaskReminder(getApplication(), savedTaskId)
            }
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun toggleTaskDone(taskWithSubTasks: TaskWithSubTasks) {
        viewModelScope.launch {
            val task = taskWithSubTasks.task
            if (task.status != TaskStatus.DONE && task.recurrence != TaskRecurrence.NONE) {
                // Recurring task completed -> advance due date to next cycle & reset
                val nextDueDate = calculateNextDueDate(task.dueDateMillis, task.recurrence)
                val updatedTask = task.copy(
                    status = TaskStatus.TODO,
                    dueDateMillis = nextDueDate
                )
                val resetSubTasks = taskWithSubTasks.subTasks.map { it.copy(isCompleted = false) }
                repository.updateTaskWithSubTasks(updatedTask, resetSubTasks)
                NotificationHelper.scheduleTaskReminder(getApplication(), updatedTask)
            } else {
                repository.toggleTaskCompleted(taskWithSubTasks)
                val updatedTask = task.copy(
                    status = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
                )
                if (updatedTask.status != TaskStatus.DONE && updatedTask.dueDateMillis != null) {
                    NotificationHelper.scheduleTaskReminder(getApplication(), updatedTask)
                } else {
                    NotificationHelper.cancelTaskReminder(getApplication(), updatedTask.id)
                }
            }
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun toggleTaskPin(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isPinned = !task.isPinned))
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun moveTaskStatus(taskWithSubTasks: TaskWithSubTasks, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = taskWithSubTasks.task
            if (newStatus == TaskStatus.DONE && task.recurrence != TaskRecurrence.NONE) {
                // Recurring task completed -> advance due date to next cycle & reset
                val nextDueDate = calculateNextDueDate(task.dueDateMillis, task.recurrence)
                val updatedTask = task.copy(
                    status = TaskStatus.TODO,
                    dueDateMillis = nextDueDate
                )
                val resetSubTasks = taskWithSubTasks.subTasks.map { it.copy(isCompleted = false) }
                repository.updateTaskWithSubTasks(updatedTask, resetSubTasks)
                NotificationHelper.scheduleTaskReminder(getApplication(), updatedTask)
            } else {
                val updatedTask = task.copy(status = newStatus)
                repository.updateTask(updatedTask)
                if (newStatus == TaskStatus.DONE) {
                    NotificationHelper.cancelTaskReminder(getApplication(), task.id)
                } else if (updatedTask.dueDateMillis != null) {
                    NotificationHelper.scheduleTaskReminder(getApplication(), updatedTask)
                }
            }
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            repository.deleteTask(task)
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    fun reorderTasks(taskPositions: List<Pair<Long, Int>>) {
        viewModelScope.launch {
            repository.reorderTasks(taskPositions)
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson(allTasks.value)
    }

    suspend fun importBackupJson(jsonString: String, replaceExisting: Boolean): Result<Int> {
        val res = repository.importBackupJson(jsonString, replaceExisting)
        TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        return res
    }

    fun clearAllData() {
        viewModelScope.launch {
            allTasks.value.forEach { item ->
                NotificationHelper.cancelTaskReminder(getApplication(), item.task.id)
            }
            repository.clearAllData()
            TaskFlowWidgetProvider.sendUpdateBroadcast(getApplication())
        }
    }

    private fun calculateNextDueDate(currentDueDateMillis: Long?, recurrence: TaskRecurrence): Long {
        val calendar = Calendar.getInstance()
        val baseTime = if (currentDueDateMillis != null && currentDueDateMillis > System.currentTimeMillis()) {
            currentDueDateMillis
        } else {
            System.currentTimeMillis()
        }
        calendar.timeInMillis = baseTime

        when (recurrence) {
            TaskRecurrence.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            TaskRecurrence.WEEKLY -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            TaskRecurrence.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            TaskRecurrence.NONE -> {}
        }
        return calendar.timeInMillis
    }
}
