package com.studiomk.ktca.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Binding<T>(
    private val getter: () -> T,
    private val setter: (T) -> Unit
) {
    var value: T
        get() = getter()
        set(newValue) = setter(newValue)

    fun get(): T = getter()
    fun set(newValue: T) = setter(newValue)
}

// MutableStateFlow<T>からBinding<T>を作成
fun <T> MutableStateFlow<T>.asBinding(): Binding<T> =
    Binding(getter = { value }, setter = { value = it })

// StateFlow<T>（immutable）はgetterのみ
fun <T> StateFlow<T>.asReadOnlyBinding(): Binding<T> =
    Binding(getter = { value }, setter = { _ -> /* no-op */ })