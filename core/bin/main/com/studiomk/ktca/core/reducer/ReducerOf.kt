package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect

interface ReducerOf<State, Action> {
    fun body(): ReducerOf<State, Action>

    fun reduce(state: State, action: Action): Pair<State, Effect<Action>> {
        return body().reduce(state, action)
    }
}
