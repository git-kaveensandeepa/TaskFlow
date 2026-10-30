package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.TaskDatabase
import com.example.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TaskFlowWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun sendUpdateBroadcast(context: Context) {
            val intent = Intent(context, TaskFlowWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, TaskFlowWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.taskflow_widget)

            // Intent to open MainActivity on widget click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Query pending tasks in background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val db = TaskDatabase.getDatabase(context)
                    val tasksWithSubs = db.taskDao().getAllTasksWithSubTasks().firstOrNull() ?: emptyList()

                    val pendingTasks = tasksWithSubs.filter { it.task.status != TaskStatus.DONE }
                        .sortedByDescending { it.task.isPinned }

                    val pendingCount = pendingTasks.size
                    views.setTextViewText(R.id.widget_task_count, "$pendingCount Pending")

                    if (pendingTasks.isEmpty()) {
                        views.setTextViewText(R.id.widget_task_line_1, "🎉 All tasks completed!")
                        views.setTextViewText(R.id.widget_task_line_2, "Tap to open TaskFlow & add tasks.")
                        views.setTextViewText(R.id.widget_task_line_3, "")
                    } else {
                        val t1 = pendingTasks.getOrNull(0)?.task
                        val t2 = pendingTasks.getOrNull(1)?.task
                        val t3 = pendingTasks.getOrNull(2)?.task

                        views.setTextViewText(
                            R.id.widget_task_line_1,
                            if (t1 != null) "• ${t1.title} (${t1.category})" else ""
                        )
                        views.setTextViewText(
                            R.id.widget_task_line_2,
                            if (t2 != null) "• ${t2.title} (${t2.category})" else ""
                        )
                        views.setTextViewText(
                            R.id.widget_task_line_3,
                            if (t3 != null) "• ${t3.title} (${t3.category})" else ""
                        )
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
