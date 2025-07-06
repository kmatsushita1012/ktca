package com.studiomk.ktca.core.store

import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.reducer.ReducerOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.emptyFlow



class Store<State, Action>(
    initialState: State,
    private val reducer: ReducerOf<State, Action>,
    private val logger: ((String) -> Unit)? = null,
) : StoreOf<State, Action> {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val runningJobs = mutableMapOf<Any, kotlinx.coroutines.Job>()

    override fun send(action: Action) {
        val (newState, effect) = reducer.reduce(_state.value, action)
        _state.value = newState

        handleEffect(effect)
    }

    private fun handleEffect(effect: Effect<Action>) {
        if (effect.children.isNotEmpty()) {
            effect.children.forEach { childEffect ->
                launchEffectJob(childEffect)
            }
        } else {
            launchEffectJob(effect)
        }
    }

    private fun launchEffectJob(effect: Effect<Action>) {
        effect.jobKey?.let { key ->
            if (isCancelEffect(effect)) {
                runningJobs[key]?.cancel()
                runningJobs.remove(key)
                return
            }

            // 既存ジョブがあればキャンセル
            runningJobs[key]?.cancel()
        }

        val job = coroutineScope.launch {
            effect.flow.collect { newAction ->
                send(newAction)
            }
        }

        effect.jobKey?.let { key ->
            runningJobs[key] = job
        }
    }

    private fun isCancelEffect(effect: Effect<*>): Boolean {
        return effect.flow == emptyFlow<Any>() && effect.jobKey != null
    }
}
