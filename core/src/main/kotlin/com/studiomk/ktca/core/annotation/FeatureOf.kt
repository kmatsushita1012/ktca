package com.studiomk.ktca.core.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class FeatureOf(val reducer: KClass<*>)