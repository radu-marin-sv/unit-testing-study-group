package com.softvision.unittestingstudygroup.exercise5

import android.content.Context
import androidx.annotation.WorkerThread
import com.softvision.unittestingstudygroup.DefaultDispatcherProvider
import com.softvision.unittestingstudygroup.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            return Repository(network, AlbumDatabase.getInstance(context).getAlbumDao(), DefaultDispatcherProvider())
        }
    }
}

class Repository(
    private val restApi: AlbumRestApi,
    private val dao: AlbumDao,
    private val dispatcherProvider: DispatcherProvider
) {
    sealed class Result {
        data class Success(val albums: List<Album>, val fromCache: Boolean) : Result()

        object TimedOut : Result()

        object NoNetwork : Result()

        data class Invalid(val statusCode: Int) : Result()
    }

    suspend fun fetch(): Result {
        return withContext(dispatcherProvider.io) {
            try {
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
    }

    suspend fun fetchOnlyFirst(): Result {
        return withContext(dispatcherProvider.io) {
            try {
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
    }

    suspend fun fetchAlbumsByUserIdList(userIdList: List<Int>): Result {
        return withContext(dispatcherProvider.default) {
            val albumList =  userIdList.flatMap { dao.getAlbumByUserId(it) }.toList()
            Result.Success(albumList.asDomainModel(), fromCache = true)
        }
    }
}