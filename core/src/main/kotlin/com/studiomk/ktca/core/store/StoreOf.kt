package com.studiomk.ktca.core.store

import com.studiomk.ktca.core.scope.KeyPath
import com.studiomk.ktca.core.scope.OptionalKeyPath
import com.studiomk.ktca.core.scope.CasePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

interface StoreOf<State, Action> {
    val state: StateFlow<State>
    fun send(action: Action)

    fun <ChildState, ChildAction> optionalScope(
        keyPath: OptionalKeyPath<State, ChildState>,
        casePath: CasePath<Action, ChildAction>,
    ): ScopedStore<ChildState,ChildAction>?{
        val current = state.value
        val childState = keyPath.get(current) ?: return null
        val childStateFlow = state
            .mapNotNull { keyPath.get(it) }
            .distinctUntilChanged()
            .stateIn(
                CoroutineScope(Dispatchers.Main),
                SharingStarted.Eagerly,
                initialValue = childState
            )
        return ScopedStore<ChildState, ChildAction>(
            state = childStateFlow,
            sendAction = { childAction ->
                this.send(casePath.inject(childAction))
            }
        )
    }

    fun <ChildState, ChildAction>scope(
        keyPath: KeyPath<State, ChildState>,
        casePath: CasePath<Action, ChildAction>,
    ): ScopedStore<ChildState, ChildAction>?{

        val current = state.value
        val childState = keyPath.get(current) ?: return null

        val childStateFlow = state
            .mapNotNull { keyPath.get(it) }
            .distinctUntilChanged()
            .stateIn(
                CoroutineScope(Dispatchers.Main),
                SharingStarted.Eagerly,
                initialValue = childState
            )
        return ScopedStore<ChildState, ChildAction>(
            state = childStateFlow,
            sendAction = { childAction ->
                this.send(casePath.inject(childAction))
            }
        )
    }
}