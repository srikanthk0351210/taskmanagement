
package com.example.taskmanager.taskdetail

import androidx.lifecycle.SavedStateHandle
import com.example.taskmanager.MainCoroutineRule
import com.example.taskmanager.R
import com.example.taskmanager.TodoDestinationsArgs
import com.example.taskmanager.data.FakeTaskRepository
import com.example.taskmanager.data.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeTaskRepository
    private val task = Task(title = "Title1", description = "Description1", id = "0",
        priority = 0, dueDate = System.currentTimeMillis())

    @Before
    fun setupViewModel() {
        tasksRepository = FakeTaskRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "0"))
        )
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() = runTest {
        val uiState = taskDetailViewModel.uiState.first()
        // Then verify that the view was notified
        assertThat(uiState.task?.title).isEqualTo(task.title)
        assertThat(uiState.task?.description).isEqualTo(task.description)
    }

    @Test
    fun completeTask() = runTest {
        // Verify that the task was active initially
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isFalse()

        // When the ViewModel is asked to complete the task
        assertThat(taskDetailViewModel.uiState.first().task?.id).isEqualTo("0")
        taskDetailViewModel.setCompleted(true)

        // Then the task is completed and the snackbar shows the correct message
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isTrue()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_complete)
    }

    @Test
    fun activateTask() = runTest {
        tasksRepository.deleteAllTasks()
        tasksRepository.addTasks(task.copy(isCompleted = true))

        // Verify that the task was completed initially
        assertThat(tasksRepository.savedTasks.value[task.id]?.isCompleted).isTrue()

        // When the ViewModel is asked to complete the task
        assertThat(taskDetailViewModel.uiState.first().task?.id).isEqualTo("0")
        taskDetailViewModel.setCompleted(false)

        // Then the task is not completed and the snackbar shows the correct message
        val newTask = tasksRepository.getTask(task.id)
        assertTrue((newTask?.isActive) ?: false)
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() = runTest {
        // Given a repository that throws errors
        tasksRepository.setShouldThrowError(true)

        // Then the task is null and the snackbar shows a loading error message
        assertThat(taskDetailViewModel.uiState.value.task).isNull()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.loading_task_error)
    }

    @Test
    fun taskDetailViewModel_taskNotFound() = runTest {
        // Given an ID for a non existent task
        taskDetailViewModel = TaskDetailViewModel(
            tasksRepository,
            SavedStateHandle(mapOf(TodoDestinationsArgs.TASK_ID_ARG to "nonexistent_id"))
        )

        // The task is null and the snackbar shows a "not found" error message
        assertThat(taskDetailViewModel.uiState.value.task).isNull()
        assertThat(taskDetailViewModel.uiState.first().userMessage)
            .isEqualTo(R.string.task_not_found)
    }

    @Test
    fun deleteTask() = runTest {
        assertThat(tasksRepository.savedTasks.value.containsValue(task)).isTrue()

        // When the deletion of a task is requested
        taskDetailViewModel.deleteTask()

        assertThat(tasksRepository.savedTasks.value.containsValue(task)).isFalse()
    }

    @Test
    fun loadTask_loading() = runTest {
        // Set Main dispatcher to not run coroutines eagerly, for just this one test
        Dispatchers.setMain(StandardTestDispatcher())

        var isLoading: Boolean? = true
        val job = launch {
            taskDetailViewModel.uiState.collect {
                isLoading = it.isLoading
            }
        }

        // Then progress indicator is shown
        assertThat(isLoading).isTrue()

        // Execute pending coroutines actions
        advanceUntilIdle()

        // Then progress indicator is hidden
        assertThat(isLoading).isFalse()
        job.cancel()
    }
}
