package com.example.taskmanager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TodoTheme(  primaryColor: Color, // Accept dynamic primary color
                darkTheme: Boolean = isSystemInDarkTheme(),
                content: @Composable () -> Unit) {

    val colors = lightColors(
        primary = primaryColor,
        primaryVariant = primaryColor,
        secondary = Color.Blue
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = primaryColor,
            secondary =primaryColor,
            tertiary = Color.Blue,
        )
    ) {
        content()
    }
}
