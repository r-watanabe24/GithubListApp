package com.example.githublistapp.di

import com.example.githublistapp.repositories.UserRepositoryImpl
import com.example.githublistapp.repositories.UserRepositoryProtocol
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepositoryProtocol
}