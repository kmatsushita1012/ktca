package com.studiomk.ktca.sample.home

import com.studiomk.ktca.core.annotation.Lens
import com.studiomk.ktca.core.annotation.Prism
import com.studiomk.ktca.core.annotation.FeatureOf
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
        @Lens val destination: DestinationState? = null,
    )

    sealed class Action {
        data class SetTitle(val title: String) : Action()
        class CounterButton1Tapped() : Action()
        class CounterButton2Tapped() : Action()
        @Prism class Destination(val action: DestinationAction) : Action()
    }

    override fun body(): ReducerOf<State, Action> =
        LetScope(
            stateLens = destinationLens,
            actionPrism = destinationPrism,
            reducer = DestinationReducer
        ) +
        Reduce<State, Action>{ state, action ->
            when (action) {
                is Action.SetTitle -> state.copy(title = action.title) to Effect.none()
                is Action.CounterButton1Tapped->{
                    destinationLens.set(state, DestinationState.Counter1(state = Counter.State(count = 0))) to Effect.none()
                }
                is Action.CounterButton2Tapped->{
                    destinationLens.set(state, DestinationState.Counter2(state = Counter.State(count = 10))) to Effect.none()
                }
                is Action.Destination -> {
//                    when {
//                        (destinationPrism + Destination.Counter1.prism).extract(action) is Counter.Action.DismissTapped -> {
//                            destinationLens.set(state, null) to Effect.none()
//                        }
//
//                        (destinationPrism + Destination.Counter2.prism).extract(action) is Counter.Action.DismissTapped -> {
//                            destinationLens.set(state, null) to Effect.none()
//                        }
//
//                        else -> state to Effect.none()
//                    }
                    when (val action = action.action) {
                        is DestinationAction.Counter1 -> {
                            when(val action = action.action){
                                is Counter.Action.DismissTapped->{
                                    state.copy(destination = null) to Effect.none()
                                }
                                else -> state to Effect.none()
                            }
                        }
                        is DestinationAction.Counter2 -> {
                            when(val action = action.action){
                                is Counter.Action.DismissTapped->{
                                    state.copy(destination = null) to Effect.none()
                                }
                                else -> state to Effect.none()
                            }
                        }
                    }
                }
            }
        }
}

