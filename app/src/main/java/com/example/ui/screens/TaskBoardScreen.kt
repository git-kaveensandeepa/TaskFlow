package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Task
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import com.example.ui.components.GlassCard
import com.example.ui.components.TaskCard
import com.example.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@Composable
fun TaskBoardScreen(
    viewModel: TaskViewModel,
    allTasks: List<TaskWithSubTasks>,
    onOpenTaskDialog: (TaskWithSubTasks?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val statuses = listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { statuses.size })

    val selectedTaskIds by viewModel.selectedTaskIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("task_board_screen")
    ) {
        // Column Tabs Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statuses.forEachIndexed { index, status ->
                val count = allTasks.count { it.task.status == status }
                val isSelected = pagerState.currentPage == index

                val statusTitle = when (status) {
                    TaskStatus.TODO -> "To Do"
                    TaskStatus.IN_PROGRESS -> "In Progress"
                    TaskStatus.DONE -> "Completed"
                }

                val badgeColor = when (status) {
                    TaskStatus.TODO -> MaterialTheme.colorScheme.primary
                    TaskStatus.IN_PROGRESS -> Color(0xFFF59E0B)
                    TaskStatus.DONE -> Color(0xFF10B981)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) badgeColor.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Pager for Board Columns
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            val currentStatus = statuses[pageIndex]
            val columnTasks = allTasks.filter { it.task.status == currentStatus }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (columnTasks.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(
                            cornerRadius = 24.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No tasks in ${
                                        when(currentStatus) {
                                            TaskStatus.TODO -> "To Do"
                                            TaskStatus.IN_PROGRESS -> "In Progress"
                                            TaskStatus.DONE -> "Done"
                                        }
                                    }",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap '+' to add a new task or drag tasks into this column.",
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
                        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                    ) {
                        itemsIndexed(
                            items = columnTasks,
                            key = { _, item -> item.task.id }
                        ) { index, item ->
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
    }
}
