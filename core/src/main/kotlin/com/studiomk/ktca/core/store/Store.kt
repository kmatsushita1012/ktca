package com.studiomk.ktca.core.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class Store<State, Action>(
    initialState: State,
    private val reducer: ReducerOf<State, Action>,
) : StoreOf<State, Action> {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun send(action: Action) {
        val (newState, effect) = reducer.reduce(_state.value, action)
        _state.value = newState
        coroutineScope.launch {
            effect.collect { newAction ->
                send(newAction)
            }
        }
    }
}