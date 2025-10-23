package com.example.githublistapp.datasource.api

import com.example.githublistapp.APIClient
import com.example.githublistapp.ApiResult
import com.example.githublistapp.datasource.api.GitHubUserDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val client: APIClient,
    private val moshi: Moshi,
) {
    private inline fun <reified T> Moshi.listAdapter() =
        Types.newParameterizedType(List::class.java, T::class.java)
            .let { adapter<List<T>>(it) }

    private inline fun <reified T> ApiResult<String>.parseListOrEmpty(moshi: Moshi): List<T> =
        when (this) {
            is ApiResult.Success -> moshi.listAdapter<T>().fromJson(data).orEmpty()
            is ApiResult.Error   -> emptyList()
        }

    suspend fun fetchUsers(since: Long, perPage: Int): List<GitHubUserDto> =
        client.get(
            path = "users",
            query = mapOf("since" to "$since", "per_page" to "$perPage")
        ).parseListOrEmpty<GitHubUserDto>(moshi)
}