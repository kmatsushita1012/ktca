package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect

fun <S, A> Reduce(block: (S, A) -> Pair<S, Effect<A>>): ReducerOf<S, A> {
    return object : ReducerOf<S, A> {
        override fun body() = this
        override fun reduce(state: S, action: A) = block(state, action)
    }
}