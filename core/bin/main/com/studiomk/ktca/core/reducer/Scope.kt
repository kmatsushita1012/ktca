package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.scope.Lens
import com.studiomk.ktca.core.scope.Prism


fun <ParentState, ParentAction, ChildState, ChildAction> Scope(
    stateLens: Lens<ParentState, ChildState>,
    actionPrism: Prism<ParentAction, ChildAction>,
    reducer: ReducerOf<ChildState, ChildAction>
): ReducerOf<ParentState, ParentAction> {
    return object : ReducerOf<ParentState, ParentAction> {
        override fun body() = this

        override fun reduce(parentState: ParentState, parentAction: ParentAction): Pair<ParentState, Effect<ParentAction>> {
            val childAction = actionPrism.extract(parentAction) ?: return parentState to Effect.none()
            val childState = stateLens.get(parentState)?: return parentState to Effect.none()

            val (newChildState, childEffect) = reducer.reduce(childState, childAction)
            val newParentState = stateLens.set(parentState, newChildState)
            val newParentEffect = childEffect.map { actionPrism.embed(it) }

            return newParentState to newParentEffect
        }
    }
}