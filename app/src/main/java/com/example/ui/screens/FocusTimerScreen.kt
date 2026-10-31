package com.example.ui.screens

import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskWithSubTasks
import com.example.ui.components.GlassCard
import com.example.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PomodoroMode(val label: String, val defaultMinutes: Int, val color: Color) {
    FOCUS("Focus Session", 25, Color(0xFF3B82F6)),
    SHORT_BREAK("Short Break", 5, Color(0xFF10B981)),
    LONG_BREAK("Long Break", 15, Color(0xFF8B5CF6))
}

@Composable
fun FocusTimerScreen(
    viewModel: TaskViewModel,
    allTasks: List<TaskWithSubTasks>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(PomodoroMode.FOCUS) }
    var totalSeconds by remember { mutableIntStateOf(selectedMode.defaultMinutes * 60) }
    var secondsRemaining by remember { mutableIntStateOf(selectedMode.defaultMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(0) }

    var selectedTaskForFocus by remember { mutableStateOf<TaskWithSubTasks?>(null) }

    // Breathing pulse glow animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isRunning) 0.65f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )
    val pulseRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.035f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRingScale"
    )

    // Synchronize timer when mode changes
    LaunchedEffect(selectedMode) {
        isRunning = false
        totalSeconds = selectedMode.defaultMinutes * 60
        secondsRemaining = totalSeconds
    }

    // Timer Countdown Engine
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            if (selectedMode == PomodoroMode.FOCUS) {
                completedSessions++
            }
            // Vibrate feedback on complete
            runCatching {
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(500L)
            }
        }
    }

    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "timerProgress"
    )

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 130.dp)
            .testTag("focus_timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Mode Selector Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PomodoroMode.values().forEach { mode ->
                val isSelected = selectedMode == mode
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) mode.color else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "modeColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) mode.color.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) mode.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            selectedMode = mode
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = animatedColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task Selection for Focus Session
        GlassCard(
            cornerRadius = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Focus Target Task:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (allTasks.isEmpty()) {
                    Text(
                        text = "No tasks available. Add tasks to link focus session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allTasks) { item ->
                            val isSelected = selectedTaskForFocus?.task?.id == item.task.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedTaskForFocus = if (isSelected) null else item
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Glass Timer Ring with Pulsing Glow Animation
        Box(contentAlignment = Alignment.Center) {
            // Breathing Glow halo
            if (isRunning) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            scaleX = pulseRingScale
                            scaleY = pulseRingScale
                            alpha = pulseGlow
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    selectedMode.color.copy(alpha = 0.45f),
                                    selectedMode.color.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            GlassCard(
                cornerRadius = 32.dp,
                elevation = 12.dp,
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        if (isRunning) {
                            scaleX = pulseRingScale
                            scaleY = pulseRingScale
                        }
                    }
                    .testTag("timer_ring_card")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = selectedMode.color,
                        strokeWidth = 12.dp,
                        trackColor = selectedMode.color.copy(alpha = 0.15f)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedMode.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = selectedMode.color,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Timer Play/Pause/Reset Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = {
                    isRunning = false
                    secondsRemaining = totalSeconds
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .testTag("reset_timer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main Play/Pause Button
            FloatingActionButton(
                onClick = { isRunning = !isRunning },
                containerColor = selectedMode.color,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .testTag("toggle_timer_btn")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip / Finish Button
            IconButton(
                onClick = {
                    isRunning = false
                    secondsRemaining = 0
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .testTag("skip_timer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip Session",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Completed Sessions Stats Badge
        GlassCard(
            cornerRadius = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = selectedMode.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Completed Focus Sessions Today",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🍅 $completedSessions Sessions Completed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
