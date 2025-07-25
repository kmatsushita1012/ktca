package com.studiomk.ktca.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.studiomk.ktca.core.util.Binding
import kotlinx.coroutines.launch

@Composable
fun <T> Sheet(
    item: T?,
    initialSnap: Float = 0.6f, // 0..1 の割合で初期高さ
    snapPoints: List<Float> = (6..20).map { it * 0.05f },
    onDismiss: (() -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable (T) -> Unit,
) {
    if (item == null) return

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    var currentHeightPx by remember { mutableFloatStateOf(screenHeightPx * initialSnap) }

    // ドラッグ状態
    val dragState = rememberDraggableState { delta ->
        val newHeight = (currentHeightPx - delta).coerceIn(
            screenHeightPx * snapPoints.first(),
            screenHeightPx * snapPoints.last()
        )
        currentHeightPx = newHeight
    }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { currentHeightPx.toDp() })
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        val closestSnap = snapPoints.minByOrNull { kotlin.math.abs(currentHeightPx / screenHeightPx - it) }
                            ?: initialSnap
                        val targetHeightPx = screenHeightPx * closestSnap

                        scope.launch {
                            animate(
                                initialValue = currentHeightPx,
                                targetValue = targetHeightPx,
                                animationSpec = tween(durationMillis = 300)
                            ) { value, _ ->
                                currentHeightPx = value
                            }
                        }
                    }
                )
                .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            content(item)
            if (dismissible) {
                BackHandler { onDismiss?.invoke() }
            }
        }
    }
}

@Composable
fun Sheet(
    isPresented: Binding<Boolean>,
    initialSnap: Float = 0.6f, // 0..1 の割合で初期高さ
    snapPoints: List<Float> = (6..20).map { it * 0.05f },
    onDismiss: (() -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable (Binding<Boolean>) -> Unit,
) {

    if (!isPresented.value) return

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    var currentHeightPx by remember { mutableFloatStateOf(screenHeightPx * initialSnap) }

    // ドラッグ状態
    val dragState = rememberDraggableState { delta ->
        val newHeight = (currentHeightPx - delta).coerceIn(
            screenHeightPx * snapPoints.first(),
            screenHeightPx * snapPoints.last()
        )
        currentHeightPx = newHeight
    }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { currentHeightPx.toDp() })
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = {
                        val closestSnap = snapPoints.minByOrNull { kotlin.math.abs(currentHeightPx / screenHeightPx - it) }
                            ?: initialSnap
                        val targetHeightPx = screenHeightPx * closestSnap

                        scope.launch {
                            animate(
                                initialValue = currentHeightPx,
                                targetValue = targetHeightPx,
                                animationSpec = tween(durationMillis = 300)
                            ) { value, _ ->
                                currentHeightPx = value
                            }
                        }
                    }
                )
                .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            content(isPresented)
            if (dismissible) {
                BackHandler { onDismiss?.invoke() }
            }
        }
    }
}