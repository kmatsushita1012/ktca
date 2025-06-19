package com.studiomk.ktca.core.reducer

import com.studiomk.ktca.core.effect.Effect
import com.studiomk.ktca.core.scope.KeyPath
import com.studiomk.ktca.core.scope.CasePath


fun <ParentState, ParentAction, ChildState, ChildAction> Scope(
    statePath: KeyPath<ParentState, ChildState>,
    actionPath: CasePath<ParentAction, ChildAction>,
    reducer: ReducerOf<ChildState, ChildAction>
): ReducerOf<ParentState, ParentAction> {
    return object : ReducerOf<ParentState, ParentAction> {
        override fun body() = this

        override fun reduce(parentState: ParentState, parentAction: ParentAction): Pair<ParentState, Effect<ParentAction>> {
            val childAction = actionPath.extract(parentAction) ?: return parentState to Effect.none()
            val childState = statePath.get(parentState)?: return parentState to Effect.none()

            val (newChildState, childEffect) = reducer.reduce(childState, childAction)
            val newParentState = statePath.set(parentState, newChildState)
            val newParentEffect = childEffect.map { actionPath.inject(it) }

            return newParentState to newParentEffect
        }
    }
}