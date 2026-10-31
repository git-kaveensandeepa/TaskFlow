package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskPriority
import com.example.data.model.TaskWithSubTasks
import com.example.ui.components.*
import com.example.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    tasks: List<TaskWithSubTasks>,
    onOpenTaskDialog: (TaskWithSubTasks?) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedPriority by viewModel.selectedPriorityFilter.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()

    val selectedTaskIds by viewModel.selectedTaskIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()

    var showCategoryCreator by remember { mutableStateOf(false) }

    if (showCategoryCreator) {
        CategoryCreatorDialog(
            onDismiss = { showCategoryCreator = false },
            onCreateCategory = { name, colorHex, iconName ->
                viewModel.addCustomCategory(name, colorHex, iconName)
                viewModel.setCategoryFilter(name)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("task_list_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        GlassTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = "Search tasks, notes, subtasks...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            testTag = "task_search_field"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        val allFilterCategories = remember(customCategories) {
            CategoryUtils.defaultCategories + customCategories.map { it.name }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allFilterCategories) { cat ->
                GlassChip(
                    text = cat,
                    isSelected = selectedCategory == cat,
                    onClick = { viewModel.setCategoryFilter(cat) },
                    accentColor = if (cat == "All") MaterialTheme.colorScheme.primary else CategoryUtils.getCategoryColor(cat),
                    testTag = "filter_category_$cat"
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { showCategoryCreator = true },
                    label = { Text("+ New") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("add_custom_category_filter")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Priority Filter Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Priority:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            TaskPriority.values().forEach { prio ->
                GlassChip(
                    text = prio.name,
                    isSelected = selectedPriority == prio,
                    onClick = { viewModel.setPriorityFilter(prio) },
                    accentColor = CategoryUtils.getPriorityColor(prio),
                    testTag = "filter_priority_${prio.name}"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(cornerRadius = 24.dp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Tasks Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All")
                                "Try adjusting your search or category filters."
                            else "Add your first task by tapping the floating '+' button!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 130.dp)
            ) {
                items(
                    items = tasks,
                    key = { item -> item.task.id }
                ) { item ->
                    val isSelected = selectedTaskIds.contains(item.task.id)
                    TaskCard(
                        taskWithSubTasks = item,
                        onToggleDone = { viewModel.toggleTaskDone(item) },
                        onClickTask = { onOpenTaskDialog(item) },
                        onDeleteTask = { viewModel.deleteTask(item.task) },
                        onTogglePin = { viewModel.toggleTaskPin(item.task) },
                        onMoveStatus = { newStatus -> viewModel.moveTaskStatus(item, newStatus) },
                        isSelected = isSelected,
                        isMultiSelectMode = isMultiSelectMode,
                        onToggleSelect = { viewModel.toggleTaskSelection(item.task.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
