package com.studiomk.ktca.sample.home

import com.studiomk.ktca.core.annotation.Cased
import com.studiomk.ktca.core.annotation.FeatureOf
import com.studiomk.ktca.core.annotation.Keyed
import com.studiomk.ktca.core.annotation.OptionalKeyed
import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.reducer.LetScope
import com.studiomk.ktca.core.reducer.Reduce
import com.studiomk.ktca.core.reducer.ReducerOf
import com.studiomk.ktca.sample.counter.Counter


object Home : ReducerOf<Home.State, Home.Action> {

    sealed class Destination {
        @FeatureOf(Counter::class)
        object Counter1 : Destination()

        @FeatureOf(Counter::class)
        object Counter2 : Destination()
    }

    data class State(
        val title: String = "Home",
        @Keyed val destination: DestinationState? = null,
    )

    sealed class Action {
        data class SetTitle(val title: String) : Action()
        class CounterButton1Tapped() : Action()
        class CounterButton2Tapped() : Action()
        @Cased class Destination(val action: DestinationAction) : Action()
    }

    override fun body(): ReducerOf<State, Action> =
        LetScope(
            keyPath = destinationKey,
            casePath = destinationCase,
            reducer = DestinationReducer
        ) +
        Reduce<State, Action>{ state, action ->
            when (action) {
                is Action.SetTitle -> state.copy(title = action.title) to Effect.none()
                is Action.CounterButton1Tapped->{
                    destinationKey.set(state, DestinationState.Counter1(state = Counter.State(count = 0))) to Effect.none()
                }
                is Action.CounterButton2Tapped->{
                    destinationKey.set(state, DestinationState.Counter2(state = Counter.State(count = 10))) to Effect.none()
                }
                is Action.Destination -> {
                    when {
                        (destinationCase + Destination.Counter1.case).extract(action) is Counter.Action.DismissTapped -> {
                            destinationKey.set(state, null) to Effect.none()
                        }

                        (destinationCase + Destination.Counter2.case).extract(action) is Counter.Action.DismissTapped -> {
                            destinationKey.set(state, null) to Effect.none()
                        }

                        else -> state to Effect.none()
                    }
                }
            }
        }
}
