package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect

interface ReducerOf<State, Action> {
    fun body(): ReducerOf<State, Action>

    fun reduce(state: State, action: Action): Pair<State, Effect<Action>> {
        return body().reduce(state, action)
    }
}

operator fun <S, A> ReducerOf<S, A>.plus(
    other: ReducerOf<S, A>
): ReducerOf<S, A> {
    return object : ReducerOf<S, A> {
        override fun body() = this
        override fun reduce(state: S, action: A): Pair<S, Effect<A>> {
            val (s1, e1) = this@plus.reduce(state, action)
            val (s2, e2) = other.reduce(s1, action)
            return s2 to Effect.merge(e1, e2)
        }
    }
}
