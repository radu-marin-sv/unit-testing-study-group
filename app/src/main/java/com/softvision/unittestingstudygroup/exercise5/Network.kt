package com.softvision.unittestingstudygroup.exercise5

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

data class NetworkAlbum(
    @Json(name = "userId") val userId: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
)

fun List<NetworkAlbum>.asDomainModels(): List<Album> {
    return map {
        Album(it.userId, it.id, it.title)
    }
}

interface AlbumRestApi {
    @GET("albums")
    fun fetchAlbums(): Response<List<NetworkAlbum>>
}

private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

val network = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    ))
    .baseUrl(BASE_URL)
    .build()
    .create(AlbumRestApi::class.java)