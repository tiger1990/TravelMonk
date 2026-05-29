package com.travelmonk.core.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PinnedClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PinnedRetrofit
