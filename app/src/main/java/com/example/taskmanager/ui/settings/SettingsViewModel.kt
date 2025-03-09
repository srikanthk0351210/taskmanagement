package com.example.taskmanager.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.util.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject  // ✅ Import this for Hilt injection

@HiltViewModel
class SettingsViewModel @Inject constructor( // ✅ Add @Inject to the constructor
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _primaryColor = MutableStateFlow(0xFF6200EE.toInt()) // Default color
    val primaryColor: StateFlow<Int> get() = _primaryColor

    init {
        // Load saved color
        viewModelScope.launch {
            userPreferencesManager.primaryColorFlow.collectLatest { color ->
                _primaryColor.value = color
            }
        }
    }

    // Save selected color
    fun savePrimaryColor(color: Int) {
        viewModelScope.launch {
            userPreferencesManager.savePrimaryColor(color)
        }
    }
}
