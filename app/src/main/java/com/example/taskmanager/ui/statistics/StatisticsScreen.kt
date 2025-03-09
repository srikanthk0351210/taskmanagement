package com.example.taskmanager.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.R
import com.example.taskmanager.util.LoadingContent
import com.example.taskmanager.util.StatisticsTopAppBar

@Composable
fun StatisticsScreen(
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { StatisticsTopAppBar(openDrawer) },
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        StatisticsContent(
            loading = uiState.isLoading,
            empty = uiState.isEmpty,
            activeTasksPercent = uiState.activeTasksPercent,
            completedTasksPercent = uiState.completedTasksPercent,
            onRefresh = { viewModel.refresh() },
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun StatisticsContent(
    loading: Boolean,
    empty: Boolean,
    activeTasksPercent: Float,
    completedTasksPercent: Float,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commonModifier = modifier
        .fillMaxSize()
        .padding(all = dimensionResource(id = R.dimen.horizontal_margin))

    LoadingContent(
        loading = loading,
        empty = empty,
        onRefresh = onRefresh,
        modifier = modifier,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.statistics_no_tasks),
                modifier = commonModifier
            )
        }
    ) {
        Column(
            commonModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!loading) {
                AnimatedCircularTaskProgress(completedTasksPercent)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.statistics_active_tasks, activeTasksPercent),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = stringResource(
                        id = R.string.statistics_completed_tasks,
                        completedTasksPercent
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Best Animated Circular Progress Indicator for Task Completion.
 */
@Composable
fun AnimatedCircularTaskProgress(completedPercentage: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = completedPercentage / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Task Progress Animation"
    )

    // Scale animation for a pulsating effect
    val scale by rememberInfiniteTransition().animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glow effect transition
    val glowAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(150.dp)
            .scale(scale)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = 14f

            // Background Circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = size.minDimension / 2f,
                style = Stroke(width = strokeWidth)
            )

            // Gradient Color Effect
            val gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF00E676), Color(0xFF1DE9B6), Color(0xFF00E5FF)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )

            // Progress Arc
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glow Effect
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = glowAlpha),
                radius = (size.minDimension / 2.5f),
                center = center
            )
        }

        // Percentage Text with Shadow
        Text(
            text = "${completedPercentage.toInt()}%",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Gray,
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Preview
@Composable
fun StatisticsContentPreview() {
    Surface {
        StatisticsContent(
            loading = false,
            empty = false,
            activeTasksPercent = 80f,
            completedTasksPercent = 20f,
            onRefresh = { }
        )
    }
}

@Preview
@Composable
fun StatisticsContentEmptyPreview() {
    Surface {
        StatisticsScreen({})
    }
}
