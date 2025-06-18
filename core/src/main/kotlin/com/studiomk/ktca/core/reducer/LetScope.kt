package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.scope.KeyPath
import com.studiomk.ktca.core.scope.CasePath
import com.studiomk.ktca.core.scope.OptionalKeyPath

fun <ParentState, ParentAction, ChildState, ChildAction> LetScope(
    stateKeyPath: OptionalKeyPath<ParentState, ChildState>,
    actionPrism: CasePath<ParentAction, ChildAction>,
    reducer: ReducerOf<ChildState, ChildAction>
): ReducerOf<ParentState, ParentAction> {
    return object : ReducerOf<ParentState, ParentAction> {
        override fun body() = this

        override fun reduce(parentState: ParentState, parentAction: ParentAction): Pair<ParentState, Effect<ParentAction>> {
            val childAction = actionPrism.extract(parentAction) ?: return parentState to Effect.none()
            val childState = stateKeyPath.get(parentState) ?: return parentState to Effect.none()

            val (newChildState, childEffect) = reducer.reduce(childState, childAction)
            val newParentState = stateKeyPath.set(parentState, newChildState)
            val newParentEffect = childEffect.map { actionPrism.inject(it) }

            return newParentState to newParentEffect
        }
    }
}