package com.example.githublistapp.datasource.api

data class GitHubUserDto(
    val id: Long,
    val login: String?,
    val avatar_url: String?,
    val html_url: String?
)