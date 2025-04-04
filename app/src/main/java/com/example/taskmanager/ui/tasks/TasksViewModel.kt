
package com.example.taskmanager.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.ADD_EDIT_RESULT_OK
import com.example.taskmanager.DELETE_RESULT_OK
import com.example.taskmanager.EDIT_RESULT_OK
import com.example.taskmanager.R
import com.example.taskmanager.data.Task
import com.example.taskmanager.data.TaskRepository
import com.example.taskmanager.tasks.TasksFilterType.ACTIVE_TASKS
import com.example.taskmanager.tasks.TasksFilterType.ALL_TASKS
import com.example.taskmanager.tasks.TasksFilterType.COMPLETED_TASKS
import com.example.taskmanager.util.Async
import com.example.taskmanager.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UiState for the task list screen.
 */
data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

/**
 * ViewModel for the task list screen.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(TASKS_FILTER_SAVED_STATE_KEY, ALL_TASKS)
    private val _sortType = MutableStateFlow(TaskSortType.PRIORITY)


    private val _filterUiInfo = _savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading = MutableStateFlow(false)
    private val _filteredTasksAsync =
        combine(taskRepository.getTasksStream(), _savedFilterType,_sortType) { tasks, type, sortType ->
            val filteredTasks = filterTasks(tasks, type)
            sortTasks(filteredTasks, sortType)
        }
            .map { Async.Success(it) }
            .catch<Async<List<Task>>> { emit(Async.Error(R.string.loading_tasks_error)) }



    private val _taskListUiState = MutableStateFlow(TasksUiState())  // MutableStateFlow
    val taskListUiState: StateFlow<TasksUiState> = _taskListUiState  // Public StateFlow

    val uiState: StateFlow<TasksUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredTasksAsync
    ) { filterUiInfo, isLoading, userMessage, tasksAsync ->
        when (tasksAsync) {
            Async.Loading -> {
                TasksUiState(isLoading = true)
            }
            is Async.Error -> {
                TasksUiState(userMessage = tasksAsync.errorMessage)
            }
            is Async.Success -> {
                TasksUiState(
                    items = tasksAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = TasksUiState(isLoading = true)
        )

    fun setFiltering(requestType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = requestType
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            taskRepository.clearCompletedTasks()
            showSnackbarMessage(R.string.completed_tasks_cleared)
            refresh()
        }
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            taskRepository.completeTask(task.id)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(task.id)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            _taskListUiState.update { currentState ->
                currentState.copy(
                    items = currentState.items.filterNot { it.id == task.id },
                    userMessage = R.string.successfully_deleted_task_message
                )
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
         //   taskRepository.saveTask(task)
            _taskListUiState.update { currentState ->
                currentState.copy(
                    items = currentState.items + task, // Add back the task
                    userMessage = R.string.successfully_added_task_message
                )
            }
        }
    }


    fun moveTask(from: Int, to: Int) {
        _taskListUiState.update { currentState ->  // _uiStateflow is mutable
            val updatedList = currentState.items.toMutableList()
            val movedItem = updatedList.removeAt(from)
            updatedList.add(to, movedItem)
            currentState.copy(items = updatedList)
        }
    }


    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            taskRepository.refresh()
            _isLoading.value = false
        }
    }

    fun setSorting(sortType: TaskSortType) {
        _sortType.value = sortType
    }

    private fun filterTasks(tasks: List<Task>, filteringType: TasksFilterType): List<Task> {
        val tasksToShow = ArrayList<Task>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                ALL_TASKS -> tasksToShow.add(task)
                ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }
                COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    private fun sortTasks(tasks: List<Task>, sortType: TaskSortType): List<Task> {
        return when (sortType) {
            TaskSortType.PRIORITY -> tasks.sortedByDescending { it.priority}   // Sort by priority value
            TaskSortType.DUE_DATE -> tasks.sortedBy { it.dueDate } // Earliest due date first
            TaskSortType.ALPHABETICAL -> tasks.sortedBy { it.title.lowercase() } // A-Z sorting
        }
    }

    private fun getFilterUiInfo(requestType: TasksFilterType): FilteringUiInfo =
        when (requestType) {
            ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )
            }
            ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp
                )
            }
            COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp
                )
            }
        }
}

// Used to save the current filtering in SavedStateHandle.
const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)
