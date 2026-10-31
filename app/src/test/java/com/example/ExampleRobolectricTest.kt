package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SubTask
import com.example.data.model.Task
import com.example.data.model.TaskPriority
import com.example.data.model.TaskRecurrence
import com.example.data.model.TaskStatus
import com.example.data.model.TaskWithSubTasks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TaskFlow", appName)
  }

  @Test
  fun `task model properties and status validation`() {
    val task = Task(
      id = 1L,
      title = "Design Animation",
      description = "Add smooth spring transitions",
      category = "Work",
      priority = TaskPriority.HIGH,
      status = TaskStatus.TODO,
      recurrence = TaskRecurrence.NONE,
      isPinned = true
    )

    assertEquals("Design Animation", task.title)
    assertEquals(TaskPriority.HIGH, task.priority)
    assertEquals(TaskStatus.TODO, task.status)
    assertTrue(task.isPinned)
  }

  @Test
  fun `subtask progress calculation in TaskWithSubTasks`() {
    val task = Task(id = 2L, title = "Project Review")
    val subTasks = listOf(
      SubTask(id = 1L, taskId = 2L, title = "Step 1", isCompleted = true),
      SubTask(id = 2L, taskId = 2L, title = "Step 2", isCompleted = false)
    )
    val taskWithSubs = TaskWithSubTasks(task, subTasks)

    val completedCount = taskWithSubs.subTasks.count { it.isCompleted }
    val totalCount = taskWithSubs.subTasks.size
    val progress = completedCount.toFloat() / totalCount.toFloat()

    assertEquals(1, completedCount)
    assertEquals(2, totalCount)
    assertEquals(0.5f, progress, 0.001f)
  }
}
