# TaskFlow 🚀 — iOS Glassmorphic ToDo & Task Manager

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Security-First](https://img.shields.io/badge/Security-100%25%20Local%20Privacy-059669?style=for-the-badge&logo=shield)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**TaskFlow** is a modern, high-performance native Android task management application featuring an **iOS 18 Glassmorphism visual design**, offline-first Room persistence, local notifications, automated recurring task schedules, an integrated Pomodoro focus timer, home screen widget sync, and JSON backup export/import capabilities.

---

## 🌟 Key Features

### 🎨 iOS Glassmorphic Visual Architecture
- **Frosted Translucent Glass Aesthetics**: Multi-layered background gradients, translucent cards, subtle shine stroke borders, and soft ambient drop shadows.
- **Light Theme Default & Dark Mode Toggle**: Crisp light-mode pastel glass default styling with a seamless toggle to deep midnight neon glass.
- **Spring Scale Micro-Interactions**: Dynamic touch press feedback (`0.98x` scale spring) across all interactive glass cards and buttons.

### 📋 Task Board & Drag-and-Drop Management
- **Kanban Board Columns**: *To Do*, *In Progress*, and *Done* statuses with task count badges.
- **Quick Status Transfers & Drag Reorder**: Tap or drag items to move across columns or reorder priorities.
- **Dynamic Checklist Subtasks**: Add subtasks to any task with animated progress percentage bars.
- **Categories & Priorities**: Categorize with *Work*, *Personal*, *Study*, *Health*, *Finance*, *Shopping*, or custom categories, alongside *High*, *Medium*, and *Low* priority badges.

### 🔔 Local Task Reminders & Notifications
- **Exact Time & Date Reminders**: Set exact due dates and reminder times with chained Date & Time Pickers.
- **Android 13+ Notification Support**: Integrated `POST_NOTIFICATIONS` runtime permission and `AlarmManager` exact alarms.
- **Auto-Cancel on Completion**: Reminder alarms automatically cancel when a task is completed or deleted.

### 🔁 Automated Recurring Tasks
- **Auto-Recurrence Schedules**: Set tasks to repeat **Daily 🔁**, **Weekly 🔁**, or **Monthly 🔁**.
- **Auto-Cycle & Subtask Reset**: Completing a recurring task automatically advances its due date to the next cycle, resets status to *To Do*, clears checklist subtasks, and reschedules the notification reminder.

### 🍅 Integrated Pomodoro Focus Timer
- **Focus Presets**: *Focus Session* (25 min), *Short Break* (5 min), and *Long Break* (15 min).
- **Task Linking**: Link timer sessions directly to active tasks.
- **Circular Progress Ring & Counter**: Animated glass countdown timer with haptic vibration upon completion and daily focus counter.

### 🎨 Custom Category Creator with Material Icons
- **12 Glass Accent Color Palette**: Choose custom accent colors for user-created categories.
- **16+ Clean Material Vector Icons**: Select from a curated set of Material icons (Star, Code, Fitness, Flight, Restaurant, Brush, Pets, Music, Camera, Tools, etc.) — strictly no emojis.

### 📱 Home Screen App Widget
- **Live Task Sync Widget**: Place a glassmorphic widget on your Android home screen to view pending tasks and total counts without launching the app.
- **Real-Time Sync**: Instant update broadcast whenever tasks are created, edited, completed, or deleted.

### 💾 Local Storage & Backup Export / Import
- **Room SQLite Offline Persistence**: 100% device-local storage ensuring complete privacy.
- **JSON Backup Export & Import**: Export full task and checklist backups to JSON format, copy to clipboard, share files, or restore/merge backups from local device storage.

### 📑 Bulk Actions & Multi-Select
- **Multi-Select Mode**: Select multiple tasks with checkboxes.
- **Floating Glass Action Bar**: Perform bulk operations — Bulk Delete, Bulk Status Move (*To Do*, *In Progress*, *Done*), and Bulk Pin/Unpin in a single tap.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Database**: Room Persistence Library (KSP Compiler, TypeConverters)
- **Concurrency**: Kotlin Coroutines & Flow (`StateFlow`, `combine`)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Notifications**: `AlarmManager`, `NotificationManagerCompat`, BroadcastReceiver
- **Home Widget**: `AppWidgetProvider`, `RemoteViews`
- **CI/CD**: GitHub Actions (`.github/workflows/build-apk.yml`)

---

## 🔒 Security & Privacy Architecture

Built adhering to **Security-First Engineering** guidelines:

- **100% Local Storage**: All tasks, notes, and subtasks remain strictly on-device in an isolated Room SQLite database.
- **Zero Cloud Leakage**: No telemetry, tracking, or unauthorized external API network transmissions.
- **Input Sanitization**: Strict JSON parsing and input validation using `org.json` with fallback safety checks.
- **Least-Privilege Permissions**: Utilizes only required local permissions (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`).

---

## 🚀 How to Build & Run Locally

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Version 17
- **Android SDK**: API 34+

### Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/TaskFlow.git
   cd TaskFlow
   ```
2. **Build Debug APK**:
   ```bash
   gradle assembleDebug
   ```
3. **Run Tests**:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## ⚙️ GitHub Actions CI/CD

An automated workflow is configured at `.github/workflows/build-apk.yml`. Every push or pull request to `main` automatically compiles and generates the debug APK artifact for download.

---

## 👤 Project Owner

Developed by **Kaveen Sandeepa**.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
