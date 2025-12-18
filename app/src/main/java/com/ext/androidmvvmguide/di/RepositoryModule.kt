package com.ext.androidmvvmguide.di

import com.ext.androidmvvmguide.data.local.UserDao
import com.ext.androidmvvmguide.data.remote.ApiService
import com.ext.androidmvvmguide.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides UserRepository
     *
     * Dependencies are automatically injected by Hilt
     */
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao
    ): UserRepository {
        return UserRepository(apiService, userDao)
    }
}