package com.example.githublistapp

import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data object Error : ApiResult<Nothing>
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Singleton
class APIClient @Inject constructor(
    private val client: OkHttpClient,
    @BaseUrl private val baseUrl: HttpUrl
) {
    private enum class Method { GET, POST, PUT, DELETE }

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val emptyJsonBody: RequestBody = "".toRequestBody(jsonMedia)

    suspend fun get(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<String> = request(Method.GET, path, query, headers, null)

    suspend fun delete(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): ApiResult<String> = request(Method.DELETE, path, query, headers, null)

    suspend fun post(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: RequestBody? = null
    ): ApiResult<String> = request(Method.POST, path, query, headers, body ?: emptyJsonBody)

    suspend fun put(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: RequestBody? = null
    ): ApiResult<String> = request(Method.PUT, path, query, headers, body ?: emptyJsonBody)

    private suspend fun request(
        method: Method,
        path: String,
        query: Map<String, String>,
        headers: Map<String, String>,
        body: RequestBody?
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder()
            .addPathSegments(path.trimStart('/'))
            .apply { query.forEach { (k, v) -> addQueryParameter(k, v) } }
            .build()

        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "githublistapp")
            .apply { headers.forEach { (k, v) -> header(k, v) } }

        when (method) {
            Method.GET    -> builder.get()
            Method.DELETE -> builder.delete()
            Method.POST   -> builder.post(body ?: emptyJsonBody)
            Method.PUT    -> builder.put(body ?: emptyJsonBody)
        }

        val req = builder.build()

        return@withContext try {
            client.newCall(req).execute().use { resp ->
                val raw = resp.body?.string()
                if (resp.isSuccessful && raw != null) {
                    ApiResult.Success(raw)
                } else {
                    ApiResult.Error
                }
            }
        } catch (_: Throwable) {
            ApiResult.Error
        }
    }
}