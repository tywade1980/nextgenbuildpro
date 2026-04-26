package com.nextgenbuildpro.di

import android.content.Context
import com.nextgenbuildpro.brain.UnifiedBrainService
import com.nextgenbuildpro.hermes.HermesRouter
import com.nextgenbuildpro.hermes.WgsRepository
import com.nextgenbuildpro.orchestrator.CarolineOrchestrator
import com.nextgenbuildpro.voice.XAIRealtimeClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideWgsRepository(@ApplicationContext ctx: Context): WgsRepository =
        WgsRepository(ctx)

    @Provides @Singleton
    fun provideUnifiedBrainService(): UnifiedBrainService = UnifiedBrainService()

    @Provides @Singleton
    fun provideXAIRealtimeClient(): XAIRealtimeClient = XAIRealtimeClient()

    @Provides @Singleton
    fun provideHermesRouter(wgsRepository: WgsRepository): HermesRouter =
        HermesRouter(wgsRepository)

    @Provides @Singleton
    fun provideCarolineOrchestrator(
        hermesRouter: HermesRouter,
        wgsRepository: WgsRepository
    ): CarolineOrchestrator = CarolineOrchestrator(hermesRouter, wgsRepository)
}
