package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect

interface ReducerOf<State, Action> {
    fun body(): ReducerOf<State, Action>

    fun reduce(state: State, action: Action): Pair<State, Effect<Action>> {
        return body().reduce(state, action)
    }

    operator fun plus(
        other: ReducerOf<State, Action>
    ): ReducerOf<State, Action> {
        val self = this
        return object : ReducerOf<State, Action> {
            override fun body() = this
            override fun reduce(state: State, action: Action): Pair<State, Effect<Action>> {

                val (s1, e1) = self.reduce(state, action)
                val (s2, e2) = other.reduce(s1, action)
                return s2 to Effect.merge(e1, e2)
            }
        }
    }
}


