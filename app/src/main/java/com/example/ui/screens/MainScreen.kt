package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            // Main Active Content with Smooth Slide & Fade Transition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                                initialOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() }
                            ) + fadeIn(animationSpec = tween(220))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                                    targetOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() }
                                ) + fadeOut(animationSpec = tween(180))
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                                initialOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() }
                            ) + fadeIn(animationSpec = tween(220))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                                    targetOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() }
                                ) + fadeOut(animationSpec = tween(180))
                            )
                        }
                    },
                    label = "screen_tab_transition",
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
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
        }

        // Animated Multi-Select Bulk Action Bar
        AnimatedVisibility(
            visible = isMultiSelectMode,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(180)
            ) + fadeOut(animationSpec = tween(150)),
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

        // Floating Action Button (+ Add Task) with Smooth Pop Animation
        AnimatedVisibility(
            visible = !isMultiSelectMode,
            enter = scaleIn(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150)),
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
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 14.dp
                ),
                modifier = Modifier.testTag("fab_add_task")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Bottom Glass Navigation Bar with Smooth Appearance
        AnimatedVisibility(
            visible = !isMultiSelectMode,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(180)
            ) + fadeOut(animationSpec = tween(150)),
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

@Composable
private fun GlassNavigationBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    // Frosted high-opacity background so cards and lists behind do not bleed through
    val navBg = if (isDark) Color(0xF21E293B) else Color(0xF6FFFFFF)
    val navBorder = if (isDark) GlassDarkCardBorder else Color(0x66FFFFFF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(navBg)
            .border(1.dp, navBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.ViewColumn,
                label = "Board",
                isSelected = activeTab == 0,
                onClick = { onTabSelected(0) },
                testTag = "nav_board",
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.FormatListBulleted,
                label = "List",
                isSelected = activeTab == 1,
                onClick = { onTabSelected(1) },
                testTag = "nav_list",
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.Timer,
                label = "Focus",
                isSelected = activeTab == 2,
                onClick = { onTabSelected(2) },
                testTag = "nav_focus",
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.PieChart,
                label = "Analytics",
                isSelected = activeTab == 3,
                onClick = { onTabSelected(3) },
                testTag = "nav_analytics",
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = activeTab == 4,
                onClick = { onTabSelected(4) },
                testTag = "nav_settings",
                modifier = Modifier.weight(1f)
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
    testTag: String,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    // Animated spring bounce for icon
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "navIconScale"
    )

    // Smooth color animation for tint and text
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "navColor"
    )

    // Smooth pill background fade
    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.16f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "navPillAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(activeColor.copy(alpha = pillAlpha))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = itemColor,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = itemColor,
                maxLines = 1
            )
        }
    }
}
