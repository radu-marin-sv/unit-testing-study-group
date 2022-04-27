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
    companion object {
        fun create(context: Context): Repository {
            return Repository(network, AlbumDatabase.getInstance(context).getAlbumDao())
        }
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
            val response = restApi.fetchAlbums().execute()
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

    @WorkerThread
    fun fetchOnlyFirst(): Result {
        return try {
            val persistedAlbums = dao.getAlbumsSorted()
            if (persistedAlbums.isEmpty()) {
                val response = restApi.fetchAlbums().execute()
                if (response.isSuccessful) {
                    val domainAlbums = response.body()?.asDomainModels() ?: listOf()
                    dao.insertAlbums(domainAlbums.asDatabaseModel())
                    Result.Success(domainAlbums, fromCache = false)
                } else {
                    Result.Invalid(response.code())
                }
            } else {
                Result.Success(persistedAlbums.asDomainModel(), fromCache = true)
            }
        } catch (exception: TimeoutException) {
            Result.TimedOut
        } catch (exception: IOException) {
            Result.NoNetwork
        }
    }

    @WorkerThread
    fun fetchAlbumsByUserIdList(userIdList: List<Int>): Result {
        val albumList =  userIdList.flatMap { dao.getAlbumByUserId(it) }.toList()
        return Result.Success(albumList.asDomainModel(), fromCache = true)
    }
}