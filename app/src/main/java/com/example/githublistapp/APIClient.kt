package com.example.githublistapp

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request

class APIClient(
    private val baseUrl: String = "https://api.github.com/",
    private val client: OkHttpClient = OkHttpClient(),
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
) {
    data class UserJson(
        val login: String?,
        val html_url: String?,
        val avatar_url: String?
    )

    fun fetchUsers(since: Int = 0, perPage: Int = 30): Result<List<UserJson>> = runCatching {
        val url = "${baseUrl}users?since=$since&per_page=$perPage"
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("HTTP ${resp.code}")
            val body = resp.body?.string().orEmpty()

            val type = Types.newParameterizedType(List::class.java, UserJson::class.java)
            val adapter = moshi.adapter<List<UserJson>>(type)
            adapter.fromJson(body).orEmpty()
        }
    }
}