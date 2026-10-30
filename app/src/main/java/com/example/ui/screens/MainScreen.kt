package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TaskWithSubTasks
import com.example.ui.components.BulkActionBar
import com.example.ui.components.TaskDialog
import com.example.ui.theme.*
import com.example.viewmodel.TaskViewModel

@Composable
fun MainScreen(
    viewModel: TaskViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()

    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedTaskIds by viewModel.selectedTaskIds.collectAsState()

    var editingTaskWithSubTasks by remember { mutableStateOf<TaskWithSubTasks?>(null) }
    var isShowTaskDialog by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        DateFormat.format("EEEE, MMM dd", System.currentTimeMillis()).toString()
    }

    // Dynamic Gradient Background
    val bgBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                GlassDarkBackgroundStart,
                GlassDarkBackgroundEnd,
                Color(0xFF030712)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                GlassLightBackgroundStart,
                GlassLightBackgroundEnd,
                Color(0xFFDBEAFE)
            )
        )
    }

    val customCategories by viewModel.customCategories.collectAsState()

    if (isShowTaskDialog) {
        TaskDialog(
            initialTaskWithSubTasks = editingTaskWithSubTasks,
            customCategories = customCategories,
            onDismiss = {
                isShowTaskDialog = false
                editingTaskWithSubTasks = null
            },
            onSaveTask = { task, subTasks ->
                viewModel.saveTask(task, subTasks)
            },
            onCreateCustomCategory = { name, colorHex, iconName ->
                viewModel.addCustomCategory(name, colorHex, iconName)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TaskFlow",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = currentDateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick Theme Toggle Icon Button
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        .testTag("quick_theme_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Mode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Main Active Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    0 -> TaskBoardScreen(
                        viewModel = viewModel,
                        allTasks = filteredTasks,
                        onOpenTaskDialog = { taskWithSubs ->
                            editingTaskWithSubTasks = taskWithSubs
                            isShowTaskDialog = true
                        }
                    )
                    1 -> TaskListScreen(
                        viewModel = viewModel,
                        tasks = filteredTasks,
                        onOpenTaskDialog = { taskWithSubs ->
                            editingTaskWithSubTasks = taskWithSubs
                            isShowTaskDialog = true
                        }
                    )
                    2 -> FocusTimerScreen(
                        viewModel = viewModel,
                        allTasks = allTasks
                    )
                    3 -> AnalyticsScreen(
                        viewModel = viewModel,
                        allTasks = allTasks
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }

        if (isMultiSelectMode) {
            // Floating Bulk Action Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                BulkActionBar(
                    selectedCount = selectedTaskIds.size,
                    onSelectAll = { viewModel.selectAllTasks(filteredTasks.map { it.task.id }) },
                    onClearSelection = { viewModel.clearTaskSelection() },
                    onBulkDelete = { viewModel.bulkDeleteSelected() },
                    onBulkMarkStatus = { status -> viewModel.bulkMarkStatus(status) },
                    onBulkTogglePin = { viewModel.bulkTogglePin(true) }
                )
            }
        } else {
            // Floating Action Button (+ Add Task)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 96.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        editingTaskWithSubTasks = null
                        isShowTaskDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom Glass Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                GlassNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            }
        }
    }
}

@Composable
private fun GlassNavigationBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val navBg = if (isDark) GlassDarkCardBg else GlassLightCardBg
    val navBorder = if (isDark) GlassDarkCardBorder else GlassLightCardBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(navBg)
            .border(1.dp, navBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.ViewColumn,
                label = "Board",
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                testTag = "nav_board"
            )
            NavItem(
                icon = Icons.Default.FormatListBulleted,
                label = "List",
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                testTag = "nav_list"
            )
            NavItem(
                icon = Icons.Default.Timer,
                label = "Focus",
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                testTag = "nav_focus"
            )
            NavItem(
                icon = Icons.Default.PieChart,
                label = "Analytics",
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                testTag = "nav_analytics"
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = activeTab == 4,
                onClick = { onTabSelected(4) },
                testTag = "nav_settings"
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(activeColor.copy(alpha = 0.18f))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
            }
        }
    }
}
