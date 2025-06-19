package com.studiomk.ktca.sample.home

import com.studiomk.ktca.core.annotation.ChildAction
import com.studiomk.ktca.core.annotation.ChildFeature
import com.studiomk.ktca.core.annotation.ChildState
import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.reducer.LetScope
import com.studiomk.ktca.core.reducer.Reduce
import com.studiomk.ktca.core.reducer.ReducerOf
import com.studiomk.ktca.sample.counter.Counter


//object Home : ReducerOf<Home.State, Home.Action> {
//
//    sealed class Destination {
//        @ChildFeature(Counter::class)
//        object Counter1 : Destination()
//
//        @ChildFeature(Counter::class)
//        object Counter2 : Destination()
//    }
//
//    data class State(
//        val title: String = "Home",
//        @ChildState val destination: DestinationState? = null,
//    )
//
//    sealed class Action {
//        data class SetTitle(val title: String) : Action()
//        class CounterButton1Tapped() : Action()
//        class CounterButton2Tapped() : Action()
//        @ChildAction class Destination(val action: DestinationAction) : Action()
//    }
//
//    override fun body(): ReducerOf<State, Action> =
//        LetScope(
//            statePath = destinationKey,
//            actionPath = destinationCase,
//            reducer = DestinationReducer
//        ) +
//        Reduce<State, Action>{ state, action ->
//            when (action) {
//                is Action.SetTitle -> state.copy(title = action.title) to Effect.none()
//                is Action.CounterButton1Tapped->{
//                    destinationKey.set(state, DestinationState.Counter1(state = Counter.State(count = 0))) to Effect.none()
//                }
//                is Action.CounterButton2Tapped->{
//                    destinationKey.set(state, DestinationState.Counter2(state = Counter.State(count = 10))) to Effect.none()
//                }
//                is Action.Destination -> {
//                    when {
//                        (destinationCase + Destination.Counter1.case).extract(action) is Counter.Action.DismissTapped -> {
//                            destinationKey.set(state, null) to Effect.none()
//                        }
//
//                        (destinationCase + Destination.Counter2.case).extract(action) is Counter.Action.DismissTapped -> {
//                            destinationKey.set(state, null) to Effect.none()
//                        }
//
//                        else -> state to Effect.none()
//                    }
//                }
//            }
//        }
//}

object Home : ReducerOf<Home.State, Home.Action> {

    data class State(
        val title: String = "Home",
        @ChildState val counter: Counter.State? = null,
    )

    sealed class Action {
        data class SetTitle(val title: String) : Action()
        class CounterButtonTapped() : Action()
        @ChildAction data class Counter(val action: Counter.Action) : Action()
    }

    override fun body(): ReducerOf<State, Action> =
        LetScope(
            statePath = counterKey,
            actionPath = counterCase,
            reducer = Counter
        ) +
        Reduce<State, Action>{ state, action ->
            when (action) {
                is Action.SetTitle -> state.copy(title = action.title) to Effect.none()
                is Action.CounterButtonTapped->{
                    counterKey.set(state, Counter.State(count = 0)) to Effect.none()
                }
                is Action.Counter -> {
                   when(val action = action.action){
                       is Counter.Action.DismissTapped -> {
                           counterKey.set(state, null) to Effect.none()
                       }
                       else -> state to Effect.none()
                   }
                }
            }
        }
}
