package com.studiomk.ktca.core.effect

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.coroutineScope

class Effect<A> private constructor(
    private val flow: Flow<A>
) {

    // map は Swift の map と同じく Effect を変換する
    fun <B> map(transform: (A) -> B): Effect<B> =
        Effect(flow.map(transform))

    suspend fun collect(collector: suspend (A) -> Unit) = flow.collect(collector)

    companion object {
        fun <A> none(): Effect<A> = Effect(emptyFlow())
        fun <A> just(value: A): Effect<A> = Effect(flowOf(value))
        fun <A> merge(vararg effects: Effect<A>): Effect<A> =
            Effect(merge(*effects.map { it.flow }.toTypedArray()))
        fun <A> run(
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            block: suspend (suspend (A) -> Unit) -> Unit
        ): Effect<A> {
            val flow = flow {
                coroutineScope {
                    block { action -> emit(action) }
                }
            }.flowOn(dispatcher)
            return Effect(flow)
        }
    }
}
