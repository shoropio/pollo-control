package com.pollocontrol.app.data.di

import android.content.Context
import com.pollocontrol.app.data.auth.AuthManager
import com.pollocontrol.app.data.cache.AppDataCache
import com.pollocontrol.app.data.local.PolloControlDatabase
import com.pollocontrol.app.data.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAuthManager(@ApplicationContext context: Context): AuthManager =
        AuthManager(context)

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager =
        SettingsManager(context)

    @Provides
    @Singleton
    fun provideAppDataCache(
        database: PolloControlDatabase,
        scope: CoroutineScope
    ): AppDataCache = AppDataCache(database, scope)
}
