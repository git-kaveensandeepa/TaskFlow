package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", 0L)
        val title = intent.getStringExtra("title") ?: "Task Reminder"
        val category = intent.getStringExtra("category") ?: "General"
        val priority = intent.getStringExtra("priority") ?: "MEDIUM"

        if (taskId != 0L) {
            NotificationHelper.showTaskNotification(
                context = context,
                taskId = taskId,
                title = title,
                category = category,
                priority = priority
            )
        }
    }
}
