package com.studiomk.ktca.core.store

import kotlinx.coroutines.flow.StateFlow

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