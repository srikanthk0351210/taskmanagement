
package com.example.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.taskmanager.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the com.example.taskmanager
 */
@AndroidEntryPoint
class TaskManagerActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val primaryColor by viewModel.primaryColor.collectAsState()
            TodoTheme(primaryColor = Color(primaryColor)) {
                TodoNavGraph()
            }
        }
    }
}
