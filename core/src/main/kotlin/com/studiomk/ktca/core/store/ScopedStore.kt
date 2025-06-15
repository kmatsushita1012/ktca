package com.studiomk.ktca.core.store

import kotlinx.coroutines.flow.StateFlow


class ScopedStore<ChildState, ChildAction>(
    override val state: StateFlow<ChildState>,
    private val sendAction: (ChildAction) -> Unit
) : StoreOf<ChildState, ChildAction> {

    override fun send(action: ChildAction) {
        sendAction(action)
    }
}
