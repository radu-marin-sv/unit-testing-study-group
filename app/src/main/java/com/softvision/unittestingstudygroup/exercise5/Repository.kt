package com.softvision.unittestingstudygroup.exercise5

import android.content.Context
import androidx.annotation.WorkerThread
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
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
    fun fetch(): Single<Result> {
//        return try {
//            val response = restApi.fetchAlbums()
//            if (response.isSuccessful) {
//                val domainAlbums = response.body()?.asDomainModels() ?: listOf()
//                dao.insertAlbums(domainAlbums.asDatabaseModel())
//                Result.Success(domainAlbums, fromCache = false)
//            } else {
//                Result.Invalid(response.code())
//            }
//        } catch (exception: TimeoutException) {
//            val persistedAlbums = dao.getAlbumsSorted()
//            if (persistedAlbums.isNotEmpty()) {
//                Result.Success(persistedAlbums.asDomainModel(), fromCache = true)
//            } else {
//                Result.TimedOut
//            }
//        } catch (exception: IOException) {
//            Result.NoNetwork
//        }

        return restApi.fetchAlbums()
            .map { it.asDomainModels() }
            .doOnSuccess { dao.insertAlbums(it.asDatabaseModel()) }
            .map<Result> { Result.Success(it, fromCache = false) }
            .onErrorResumeNext {
                when (it) {
                    is HttpException -> {
                        Single.just(Result.Invalid(it.code()))
                    }
                    is TimeoutException -> {
                        dao.getAlbumsSorted()
                            .flatMap { persistedAlbums ->
                                if (persistedAlbums.isNotEmpty()) {
                                    Single.just(
                                        Result.Success(
                                            persistedAlbums.asDomainModel(),
                                            fromCache = true
                                        )
                                    )
                                } else {
                                    Single.just(Result.TimedOut)
                                }
                            }

                    }
                    is IOException -> Single.just(Result.NoNetwork)
                    else -> Single.error(it)
                }
            }

    }

    @WorkerThread
    fun fetchOnlyFirst(): Single<Result> {
//        return try {
//            val persistedAlbums = dao.getAlbumsSorted()
//            if (persistedAlbums.isEmpty()) {
//                val response = restApi.fetchAlbums().execute()
//                if (response.isSuccessful) {
//                    val domainAlbums = response.body()?.asDomainModels() ?: listOf()
//                    dao.insertAlbums(domainAlbums.asDatabaseModel())
//                    Result.Success(domainAlbums, fromCache = false)
//                } else {
//                    Result.Invalid(response.code())
//                }
//            } else {
//                Result.Success(persistedAlbums.asDomainModel(), fromCache = true)
//            }
//        } catch (exception: TimeoutException) {
//            Result.TimedOut
//        } catch (exception: IOException) {
//            Result.NoNetwork
//        }

        return dao.getAlbumsSorted()
            .flatMap { persistedAlbums ->
                if (persistedAlbums.isEmpty()) {
                    restApi.fetchAlbums()
                        .map {
                            it.asDomainModels()
                        }
                        .doOnSuccess {
                            dao.insertAlbums(it.asDatabaseModel())
                        }
                        .map<Result> { Result.Success(it, fromCache = false) }
                        .onErrorResumeNext {
                            when(it) {
                                is HttpException -> {
                                    Single.just(Result.Invalid(it.code()))
                                }
                                is TimeoutException -> {
                                    Single.just(Result.TimedOut)
                                }
                                is IOException -> Single.just(Result.NoNetwork)
                                else -> Single.error(it)                           }
                        }
                } else {
                    Single.just(Result.Success(persistedAlbums.asDomainModel(), fromCache = true))
                }
            }
    }

    @WorkerThread
    fun fetchAlbumsByUserIdList(userIdList: List<Int>): Single<Result> {
//        val albumList =  userIdList.flatMap { dao.getAlbumByUserId(it) }.toList()
//        return Result.Success(albumList.asDomainModel(), fromCache = true)
        return Observable
            .fromIterable(userIdList)
            .flatMapSingle { dao.getAlbumByUserId(it) }
            .collectInto(mutableListOf<DatabaseAlbum>()) { allAlbums, albumsByUserId -> allAlbums.addAll(albumsByUserId)}
            .map {
                Result.Success(it.asDomainModel(), fromCache = true)
            }
    }
}