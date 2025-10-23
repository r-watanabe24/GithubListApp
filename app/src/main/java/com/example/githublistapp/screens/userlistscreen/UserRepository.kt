package com.example.githublistapp.screens.userlistscreen

import com.example.githublistapp.datasource.api.UserRemoteDataSource
import javax.inject.Inject

interface UserRepositoryProtocol {
    suspend fun fetchUsers(since: Long, perPage: Int): List<UserEntity>
}

data class UserEntity(
    val id: Long,
    val name: String,
    val avatar: String,
    val url: String
)

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