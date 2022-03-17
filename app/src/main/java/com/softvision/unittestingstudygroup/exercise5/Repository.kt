package com.softvision.unittestingstudygroup.exercise5

import android.content.Context
import androidx.annotation.WorkerThread
import java.io.IOException
import java.util.concurrent.TimeoutException

data class Album(
    val userId: Int,
    val id: Int,
    val title: String
)

class RepositoryFactory {
    fun create(context: Context): Repository {
        return Repository(network, AlbumDatabase.getInstance(context).getAlbumDao())
    }
}

class Repository(
    private val restApi: AlbumRestApi,
    private val dao: AlbumDao
) {
    sealed class Result {
        data class Success(val albums: List<Album>, val fromCache: Boolean) : Result()

        object TimedOut : Result()

        object NoNetwork : Result()

        data class Invalid(val statusCode: Int) : Result()
    }

    @WorkerThread
    fun fetch(): Result {
        return try {
            val response = restApi.fetchAlbums()
            if (response.isSuccessful) {
                val domainAlbums = response.body()?.asDomainModels() ?: listOf()
                dao.insertAlbums(domainAlbums.asDatabaseModel())
                Result.Success(domainAlbums, fromCache = false)
            } else {
                Result.Invalid(response.code())
            }
        } catch (exception: TimeoutException) {
            val persistedAlbums = dao.getAlbumsSorted()
            if (persistedAlbums.isNotEmpty()) {
                Result.Success(persistedAlbums.asDomainModel(), fromCache = true)
            } else {
                Result.TimedOut
            }
        } catch (exception: IOException) {
            Result.NoNetwork
        }
    }
}