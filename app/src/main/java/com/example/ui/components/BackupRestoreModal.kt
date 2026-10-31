package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BackupRestoreModal(
    onDismiss: () -> Unit,
    onExportJson: () -> String,
    onImportJson: (String, Boolean) -> Result<Int>
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Export, 1 = Import

    // Export State
    val exportedJson = remember { onExportJson() }

    // Import State
    var importText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(false) }

    // File Picker Launcher for JSON import
    val jsonFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            runCatching {
                context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.bufferedReader().use { reader -> reader.readText() }
                }
            }.onSuccess { text: String? ->
                if (!text.isNullOrBlank()) {
                    importText = text
                    importStatusMessage = "File loaded successfully (${text.length} bytes)"
                    isImportSuccess = true
                } else {
                    importStatusMessage = "Selected file is empty or unreadable."
                    isImportSuccess = false
                }
            }.onFailure { error ->
                importStatusMessage = "Failed to read file: ${error.localizedMessage}"
                isImportSuccess = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val isDark = MaterialTheme.colorScheme.background.red < 0.2f
        val dialogBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E293B) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)

        GlassCard(
            cornerRadius = 24.dp,
            elevation = 16.dp,
            backgroundColor = dialogBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("backup_restore_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Backup & Restore Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_backup_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector (Export vs Import)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("backup_tabs")
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Export Data") },
                        icon = { Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("export_tab")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Import Data") },
                        icon = { Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("import_tab")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // EXPORT TAB
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export your tasks and checklists as JSON format for offline backup or migration.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // JSON Output Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = exportedJson,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copy Button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("TaskFlow Backup", exportedJson)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_json_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy JSON")
                            }

                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "TaskFlow Backup Data")
                                        putExtra(Intent.EXTRA_TEXT, exportedJson)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export TaskFlow Backup"))
                                },
                                modifier = Modifier.weight(1f).testTag("share_json_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share File")
                            }
                        }
                    }
                } else {
                    // IMPORT TAB
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paste JSON backup content or pick a JSON file from your local storage to restore.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // File picker button
                        OutlinedButton(
                            onClick = { jsonFilePickerLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth().testTag("pick_file_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick JSON File from Device")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Text Area for manual paste
                        GlassTextField(
                            value = importText,
                            onValueChange = {
                                importText = it
                                importStatusMessage = null
                            },
                            placeholder = "Or paste JSON backup text here...",
                            singleLine = false,
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            testTag = "import_json_input"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Replace vs Merge Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = replaceExisting,
                                onCheckedChange = { replaceExisting = it },
                                modifier = Modifier.testTag("replace_existing_checkbox")
                            )
                            Text(
                                text = "Replace all existing data",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (importStatusMessage != null) {
                            Text(
                                text = importStatusMessage!!,
                                color = if (isImportSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Import Action Button
                        Button(
                            onClick = {
                                if (importText.isBlank()) {
                                    importStatusMessage = "Please paste or pick a JSON file first."
                                    isImportSuccess = false
                                    return@Button
                                }

                                val result = onImportJson(importText, replaceExisting)
                                if (result.isSuccess) {
                                    val count = result.getOrDefault(0)
                                    importStatusMessage = "Successfully imported $count tasks!"
                                    isImportSuccess = true
                                    Toast.makeText(context, "Imported $count tasks!", Toast.LENGTH_SHORT).show()
                                } else {
                                    importStatusMessage = "Import failed: ${result.exceptionOrNull()?.message}"
                                    isImportSuccess = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_import_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore / Import Tasks", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

