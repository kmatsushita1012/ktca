package com.studiomk.ktca.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun <T> FullScreenNavigation(
    item: T?,
    onDismiss: (() -> Unit)? = null,
    dismissable: Boolean = true,
    content: @Composable (T) -> Unit
) {
    var visibleItem by remember { mutableStateOf<T?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // item が来たとき表示開始
    LaunchedEffect(item) {
        if (item != null) {
            visibleItem = item
            isVisible = true
        } else if (visibleItem != null) {
            isVisible = false // 非表示アニメ開始
        }
    }

    val transition = updateTransition(isVisible, label = "fullscreen-transition")
    val alpha by transition.animateFloat(label = "alpha") { shown -> if (shown) 1f else 0f }
    val offsetY by transition.animateDp(label = "offsetY") { shown -> if (shown) 0.dp else 40.dp }

    // アニメ完了後に完全に消す
    LaunchedEffect(isVisible) {
        if (!isVisible && visibleItem != null) {
            delay(200) // アニメーションに合わせて
            visibleItem = null
            onDismiss?.invoke()
        }
    }

    if (visibleItem != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha
                    translationY = offsetY.toPx()
                }
                .background(Color.White)
        ) {
            content(visibleItem!!)

            if (dismissable) {
                BackHandler { onDismiss?.invoke() }
            }
        }
    }
}

@Composable
fun <T> SheetNavigation(
    item: T?,
    initialSnap: Float = 0.6f, // 0..1 の割合で初期高さ
    snapPoints: List<Float> = (6..20).map { it * 0.05f },
    onDismiss: (() -> Unit)? = null,
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
        modifier = Modifier.fillMaxSize(),
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
        }
    }
}
