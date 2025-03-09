# Task Manager App

## Overview
Task Manager is a modern Android application built using **Jetpack Compose**, following **MVVM Clean Architecture** principles. It allows users to create, manage, and track their tasks with a sleek UI that follows **Material Design 3** guidelines.

## Features

### ✅ Core Functionalities
- **Task Creation**: Add tasks with:
    - Title (Required)
    - Description (Optional)
    - Priority Levels (Low, Medium, High)
    - Due Date (Date Picker)
- **Task List**:
    - Sort by **priority, due date, or title**.
    - Filter tasks by **All, Completed, Pending**.
- **Task Details**:
    - View details of a task.
    - Mark tasks as completed.
    - Delete tasks.
- **Persistent Storage**:
    - Uses **Room Database** to store tasks locally.

### 🎨 UI/UX Features
- **Jetpack Compose UI**:
    - **Material Design 3 Components**: TopAppBar, Card, FloatingActionButton, etc.
    - **Adaptive Layout** for different screen sizes (Phones & Tablets).
- **Dynamic Theming**:
    - **Light & Dark Mode** support.
    - Customizable primary color via the **Settings Screen**.
- **Smooth Animations**:
    - Slide-in/slide-out effects when adding/removing tasks.
    - Circular reveal animation for the task details screen.
    - Subtle bounce effect for the FAB button.

### 🔥 Advanced Features
- **Drag & Drop**: Reorder tasks in the list.
- **Swipe Gestures**:
    - **Swipe to delete**.
    - **Swipe to mark as completed** (with undo option).
- **Progress Indicator**:
    - Circular progress bar showing completed tasks.
- **Engaging Empty State UI**:
    - Displays an illustration & motivational message when no tasks exist.

### ♿ Accessibility
- Proper **content descriptions** for screen readers.
- Supports **large text scaling** and **high-contrast mode**.
- Keyboard navigation for task creation and interaction.

### 🚀 Performance & Optimizations
- Uses **LazyColumn** for smooth scrolling with **100+ tasks**.
- Efficient recomposition with **remember** and **LaunchedEffect**.
- **Shimmer loading effect** while data loads.

### 🧪 Testing
- **Unit Tests**:
    - ViewModel logic (e.g., `AddEditTaskViewModelTest.kt`).
- **UI Tests (Compose Testing)**:
    - Task creation flow.
    - Sorting & filtering functionality.
    - Animation triggers.
- **Screenshot Testing**:
    - Validates UI across Light & Dark modes.

## 📂 Project Structure
com.example.taskmanager │── data │ ├── source │ │ ├── TaskRepository.kt │ │ ├── DefaultTaskRepository.kt │ │ ├── Task.kt │ │ ├── ModelMappingExt.kt │── di │ ├── CoroutinesModule.kt │ ├── DataModules.kt │── tasks │ ├── TasksViewModel.kt │ ├── TasksScreen.kt │── ui │ ├── addedittask │ ├── settings │ ├── statistics │ ├── taskdetail │── util │ ├── TaskManagerActivity.kt │ ├── TaskManagerApplication.kt │── androidTest │ ├── addedittask/AddEditTaskScreenTest.kt │ ├── data/source/local/TaskDaoTest.kt │ ├── statistics/StatisticsScreenTest.kt

## 🛠 Tech Stack
- **Kotlin 2.x**
- **Jetpack Compose 1.6.x**
- **Android SDK 34/35**
- **Room Database**
- **Hilt (Dependency Injection)**
- **Flow & Coroutines**
- **Navigation Component (Compose)**
- **Material Design 3**
- **Compose Testing Library**
- **Mockk (Unit Testing)**