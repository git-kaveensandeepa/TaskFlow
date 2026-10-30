package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskRecurrence
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import java.util.*

@Composable
fun TaskDialog(
    initialTaskWithSubTasks: TaskWithSubTasks? = null,
    customCategories: List<com.example.data.model.Category> = emptyList(),
    onDismiss: () -> Unit,
    onSaveTask: (Task, List<SubTask>) -> Unit,
    onCreateCustomCategory: ((name: String, colorHex: String, iconName: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isEdit = initialTaskWithSubTasks != null

    var title by remember { mutableStateOf(initialTaskWithSubTasks?.task?.title ?: "") }
    var description by remember { mutableStateOf(initialTaskWithSubTasks?.task?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(initialTaskWithSubTasks?.task?.category ?: "Work") }
    var selectedPriority by remember { mutableStateOf(initialTaskWithSubTasks?.task?.priority ?: TaskPriority.MEDIUM) }
    var selectedStatus by remember { mutableStateOf(initialTaskWithSubTasks?.task?.status ?: TaskStatus.TODO) }
    var selectedRecurrence by remember { mutableStateOf(initialTaskWithSubTasks?.task?.recurrence ?: TaskRecurrence.NONE) }
    var dueDateMillis by remember { mutableStateOf(initialTaskWithSubTasks?.task?.dueDateMillis) }
    var isPinned by remember { mutableStateOf(initialTaskWithSubTasks?.task?.isPinned ?: false) }

    var showCategoryCreator by remember { mutableStateOf(false) }

    if (showCategoryCreator) {
        CategoryCreatorDialog(
            onDismiss = { showCategoryCreator = false },
            onCreateCategory = { name, colorHex, iconName ->
                onCreateCustomCategory?.invoke(name, colorHex, iconName)
                selectedCategory = name
            }
        )
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val subTaskList = remember {
        mutableStateListOf<SubTask>().apply {
            if (initialTaskWithSubTasks != null) {
                addAll(initialTaskWithSubTasks.subTasks)
            }
        }
    }
    var newSubTaskTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            cornerRadius = 24.dp,
            elevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Task" else "Create Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_task_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Task Title
                    item {
                        Text(
                            text = "Title *",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                errorMessage = null
                            },
                            placeholder = "Enter task title...",
                            testTag = "task_dialog_title_input"
                        )
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Task Description
                    item {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        GlassTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Add notes or details...",
                            singleLine = false,
                            maxLines = 3,
                            testTag = "task_dialog_desc_input"
                        )
                    }

                    // Category Selector
                    item {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val allCategoryNames = remember(customCategories) {
                            CategoryUtils.defaultCategories.filter { it != "All" } + customCategories.map { it.name }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allCategoryNames) { cat ->
                                GlassChip(
                                    text = cat,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    accentColor = CategoryUtils.getCategoryColor(cat),
                                    testTag = "dialog_category_$cat"
                                )
                            }
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = { showCategoryCreator = true },
                                    label = { Text("+ New Category") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.testTag("add_custom_category_chip")
                                )
                            }
                        }
                    }

                    // Priority Selector
                    item {
                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskPriority.values().forEach { prio ->
                                GlassChip(
                                    text = prio.name,
                                    isSelected = selectedPriority == prio,
                                    onClick = { selectedPriority = prio },
                                    accentColor = CategoryUtils.getPriorityColor(prio),
                                    testTag = "dialog_priority_${prio.name}"
                                )
                            }
                        }
                    }

                    // Status Selector
                    item {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskStatus.values().forEach { stat ->
                                GlassChip(
                                    text = when(stat) {
                                        TaskStatus.TODO -> "To Do"
                                        TaskStatus.IN_PROGRESS -> "In Progress"
                                        TaskStatus.DONE -> "Done"
                                    },
                                    isSelected = selectedStatus == stat,
                                    onClick = { selectedStatus = stat },
                                    testTag = "dialog_status_${stat.name}"
                                )
                            }
                        }
                    }

                    // Repeat / Recurrence Selector
                    item {
                        Text(
                            text = "Repeat / Auto-Recurrence",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TaskRecurrence.values().forEach { rec ->
                                GlassChip(
                                    text = when(rec) {
                                        TaskRecurrence.NONE -> "None"
                                        TaskRecurrence.DAILY -> "Daily 🔁"
                                        TaskRecurrence.WEEKLY -> "Weekly 🔁"
                                        TaskRecurrence.MONTHLY -> "Monthly 🔁"
                                    },
                                    isSelected = selectedRecurrence == rec,
                                    onClick = { selectedRecurrence = rec },
                                    testTag = "dialog_recurrence_${rec.name}"
                                )
                            }
                        }
                    }

                    // Due Date & Time Picker Row
                    item {
                        Column {
                            Text(
                                text = "Reminder & Due Date",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        }

                                        val calendar = Calendar.getInstance()
                                        val current = dueDateMillis
                                        if (current != null) {
                                            calendar.timeInMillis = current
                                        }

                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val selectedCal = Calendar.getInstance()
                                                selectedCal.set(Calendar.YEAR, year)
                                                selectedCal.set(Calendar.MONTH, month)
                                                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                                // Chain TimePickerDialog
                                                TimePickerDialog(
                                                    context,
                                                    { _, hourOfDay, minute ->
                                                        selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                        selectedCal.set(Calendar.MINUTE, minute)
                                                        selectedCal.set(Calendar.SECOND, 0)
                                                        dueDateMillis = selectedCal.timeInMillis
                                                    },
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    calendar.get(Calendar.MINUTE),
                                                    false
                                                ).show()
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("pick_due_date")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val dateVal = dueDateMillis
                                    Text(
                                        text = if (dateVal != null) {
                                            android.text.format.DateFormat.format("MMM dd, yyyy 'at' hh:mm a", dateVal).toString()
                                        } else "Set Reminder Time"
                                    )
                                }

                                if (dueDateMillis != null) {
                                    IconButton(onClick = { dueDateMillis = null }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Reminder",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                FilterChip(
                                    selected = isPinned,
                                    onClick = { isPinned = !isPinned },
                                    label = { Text(if (isPinned) "Pinned" else "Pin") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("toggle_pinned_dialog")
                                )
                            }
                        }
                    }

                    // Checklist / Subtasks
                    item {
                        Text(
                            text = "Checklist Subtasks",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GlassTextField(
                                value = newSubTaskTitle,
                                onValueChange = { newSubTaskTitle = it },
                                placeholder = "Add subtask item...",
                                modifier = Modifier.weight(1f),
                                testTag = "subtask_title_input"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newSubTaskTitle.isNotBlank()) {
                                        subTaskList.add(
                                            SubTask(
                                                taskId = initialTaskWithSubTasks?.task?.id ?: 0,
                                                title = newSubTaskTitle.trim()
                                            )
                                        )
                                        newSubTaskTitle = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("add_subtask_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add subtask",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    items(subTaskList) { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.isCompleted,
                                onCheckedChange = { checked ->
                                    val index = subTaskList.indexOf(sub)
                                    if (index >= 0) {
                                        subTaskList[index] = sub.copy(isCompleted = checked)
                                    }
                                }
                            )
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { subTaskList.remove(sub) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove subtask",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save / Submit Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Task title cannot be empty"
                            return@Button
                        }

                        val taskToSave = Task(
                            id = initialTaskWithSubTasks?.task?.id ?: 0,
                            title = title.trim(),
                            description = description.trim(),
                            category = selectedCategory,
                            priority = selectedPriority,
                            status = selectedStatus,
                            recurrence = selectedRecurrence,
                            dueDateMillis = dueDateMillis,
                            isPinned = isPinned,
                            createdAt = initialTaskWithSubTasks?.task?.createdAt ?: System.currentTimeMillis(),
                            positionIndex = initialTaskWithSubTasks?.task?.positionIndex ?: 0
                        )

                        onSaveTask(taskToSave, subTaskList.toList())
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_task_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isEdit) "Save Changes" else "Create Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
