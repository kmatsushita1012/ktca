package com.studiomk.ktca.sample.counter

import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.reducer.Reduce
import com.studiomk.ktca.core.reducer.ReducerOf

object Counter : ReducerOf<Counter.State, Counter.Action> {

    data class State(val count: Int = 0)

    sealed class Action {
        object IncrementTapped : Action()
        object DecrementTapped : Action()
        object DismissTapped : Action()
    }

    override fun body(): ReducerOf<State, Action> =
        Reduce { state, action ->
            when (action) {
                is Action.IncrementTapped -> state.copy(count = state.count + 1) to Effect.none()
                is Action.DecrementTapped -> state.copy(count = state.count - 1) to Effect.none()
                is Action.DismissTapped -> state to Effect.none()
            }
        }
}
