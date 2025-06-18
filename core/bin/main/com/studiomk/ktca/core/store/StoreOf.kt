package com.studiomk.ktca.core.store

import com.studiomk.ktca.core.scope.Lens
import com.studiomk.ktca.core.scope.Prism
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

    fun <ChildState, ChildAction>scope(
        lens: Lens<State, ChildState?>,
        prism: Prism<Action, ChildAction>,
    ): ScopedStore<ChildState, ChildAction>?{

        val current = state.value
        val childState = lens.get(current) ?: return null

        val childStateFlow = state
            .mapNotNull { lens.get(it) }
            .distinctUntilChanged()
            .stateIn(
                CoroutineScope(Dispatchers.Main),
                SharingStarted.Eagerly,
                initialValue = childState
            )
        return ScopedStore<ChildState, ChildAction>(
            state = childStateFlow,
            sendAction = { childAction ->
                this.send(prism.embed(childAction))
            }
        )
    }
}