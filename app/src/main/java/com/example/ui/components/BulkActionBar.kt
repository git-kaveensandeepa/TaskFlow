package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskStatus
import com.example.ui.theme.GlassDarkCardBg
import com.example.ui.theme.GlassDarkCardBorder
import com.example.ui.theme.GlassLightCardBg
import com.example.ui.theme.GlassLightCardBorder

@Composable
fun BulkActionBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBulkDelete: () -> Unit,
    onBulkMarkStatus: (TaskStatus) -> Unit,
    onBulkTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val navBg = if (isDark) GlassDarkCardBg else GlassLightCardBg
    val navBorder = if (isDark) GlassDarkCardBorder else GlassLightCardBorder

    var showStatusMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(navBg)
            .border(1.5.dp, navBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("bulk_action_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClearSelection,
                modifier = Modifier.testTag("cancel_multi_select")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Selection",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$selectedCount Selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            // Select All Button
            IconButton(
                onClick = onSelectAll,
                modifier = Modifier.testTag("select_all_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Select All",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Pin/Unpin Button
            IconButton(
                onClick = onBulkTogglePin,
                modifier = Modifier.testTag("bulk_pin_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pin Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Status Dropdown
            Box {
                IconButton(
                    onClick = { showStatusMenu = true },
                    modifier = Modifier.testTag("bulk_status_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Change Status",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Move to To Do") },
                        onClick = {
                            showStatusMenu = false
                            onBulkMarkStatus(TaskStatus.TODO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to In Progress") },
                        onClick = {
                            showStatusMenu = false
                            onBulkMarkStatus(TaskStatus.IN_PROGRESS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mark as Done") },
                        onClick = {
                            showStatusMenu = false
                            onBulkMarkStatus(TaskStatus.DONE)
                        }
                    )
                }
            }

            // Delete Selected
            IconButton(
                onClick = onBulkDelete,
                modifier = Modifier.testTag("bulk_delete_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Selected",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
