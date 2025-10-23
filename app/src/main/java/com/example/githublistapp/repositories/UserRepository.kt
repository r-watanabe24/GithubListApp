package com.example.githublistapp.repositories

import com.example.githublistapp.datasource.api.UserRemoteDataSource
import javax.inject.Inject

interface UserRepositoryProtocol {
    suspend fun fetchUsers(since: Long, perPage: Int): List<UserEntity>
}

class UserRepositoryImpl @Inject constructor(
    private val remote: UserRemoteDataSource
) : UserRepositoryProtocol {
    override suspend fun fetchUsers(since: Long, perPage: Int): List<UserEntity> {
        return remote.fetchUsers(since, perPage).map {
            UserEntity(
                id = it.id,
                name = it.login ?: "(no name)",
                avatar = it.avatar_url.orEmpty(),
                url = it.html_url.orEmpty()
            )
        }
    }
}