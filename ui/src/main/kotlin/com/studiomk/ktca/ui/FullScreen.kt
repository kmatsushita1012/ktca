package com.studiomk.ktca.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.studiomk.ktca.core.util.Binding

@Composable
fun <T> FullScreen(
    item: T?,
    onDismiss: (() -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable (T) -> Unit
) {
    var visibleItem by remember { mutableStateOf<T?>(null) }

    LaunchedEffect(item) {
        if (item != null) {
            visibleItem = item
        } else if (visibleItem != null) {
            // item が null になったら非表示 → 消す
            delay(200)
            visibleItem = null
        }
    }

    val transition = updateTransition(targetState = item != null, label = "fullscreen-transition")
    val alpha by transition.animateFloat(label = "alpha") { if (it) 1f else 0f }
    val offsetY by transition.animateDp(label = "offsetY") { if (it) 0.dp else 40.dp }

    if (visibleItem != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha
                    translationY = offsetY.toPx()
                }
                .background(Color.White)
                .pointerInput(Unit) {},
        ) {
            content(visibleItem!!)

            if (dismissible) {
                BackHandler { onDismiss?.invoke() }
            }
        }
    }
}

@Composable
fun FullScreen(
    isPresented: Binding<Boolean>,
    onDismiss: (() -> Unit)? = null,
    dismissible: Boolean = true,
    content: @Composable (Binding<Boolean>) -> Unit
) {

    val transition = updateTransition(isPresented.value, label = "fullscreen-transition")
    val alpha by transition.animateFloat(label = "alpha") { shown -> if (shown) 1f else 0f }
    val offsetY by transition.animateDp(label = "offsetY") { shown -> if (shown) 0.dp else 40.dp }

    // アニメ完了後に完全に消す
    LaunchedEffect(isPresented.value) {
        if (!isPresented.value) {
            delay(200) // アニメーションに合わせて
        }
    }

    if (isPresented.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha
                    translationY = offsetY.toPx()
                }
                .background(Color.White)
                .pointerInput(Unit) {}
        ) {
            content(isPresented)

            if (dismissible) {
                BackHandler { onDismiss?.invoke() }
            }
        }
    }
}