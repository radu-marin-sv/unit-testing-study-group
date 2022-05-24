package com.softvision.unittestingstudygroup.exercise5

import com.nhaarman.mockitokotlin2.*
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

@RunWith(MockitoJUnitRunner::class)
class MockitoRepositoryTest {

    @Mock
    private lateinit var albumRestApi: AlbumRestApi
    @Mock
    private lateinit var albumDao: AlbumDao

    @InjectMocks
    private lateinit var repository: Repository

    @Test
    fun `given a server replied with data and we don't have nothing stored, when fetching data, then the fetch methods are called in expected order`() {
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.just(NETWORK_ALBUMS))
        whenever(albumDao.getAlbumsSorted()).thenReturn(Single.just(listOf()))

        repository.fetchOnlyFirst().test()

        val fetchFirstOrder = inOrder(albumDao, albumRestApi)
        fetchFirstOrder.verify(albumDao).getAlbumsSorted()
        fetchFirstOrder.verify(albumRestApi).fetchAlbums()
        fetchFirstOrder.verify(albumDao).insertAlbums(anyList())
    }

    @Test
    fun `given, when, then`() {
        // given
        whenever(albumDao.getAlbumByUserId(any()))
            .thenReturn(
                Single.just(DATABASE_ALBUMS),
                Single.just(DATABASE_ALBUMS_USER1),
                Single.just(DATABASE_ALBUMS_USER2)
            )

        // when
        val testObserver = repository.fetchAlbumsByUserIdList(listOf(0, 1, 2)).test()

        // then
        val albumList =listOf(DOMAIN_ALBUMS, DOMAIN_ALBUMS_USER1, DOMAIN_ALBUMS_USER2).flatten()
//        assertThat(testObserver, equalTo(Repository.Result.Success(albumList, true)))
        testObserver
            .assertValue(Repository.Result.Success(albumList, true))
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun `given the server replied with data, when fetching data, the the data will be successfully fetched`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.just(NETWORK_ALBUMS))

        // when
        val testObserver = repository.fetch().test()

        // then
        val captorAlbum = argumentCaptor<List<DatabaseAlbum>>()
        verify(albumDao).insertAlbums(captorAlbum.capture())
        assertThat(captorAlbum.firstValue, equalTo(DATABASE_ALBUMS))

        testObserver
            .assertComplete()
            .assertNoErrors()
            .assertValue(Repository.Result.Success(DOMAIN_ALBUMS, fromCache = false))
    }

    @Test
    fun `given the server replied with an invalid status code, when fetching data, the the status code will be reported`() {
        // given
        val error = Response.error<String>(404, ResponseBody.create(MediaType.parse(""), ""))
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.error(HttpException(error)))

        // when
        val testObserver = repository.fetch().test()

        // then
        testObserver.assertValue(Repository.Result.Invalid(404))

        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server timed out and there was not data in the cache, when fetching data, the the time out will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.error(TimeoutException()))
        whenever(albumDao.getAlbumsSorted()).thenReturn(Single.just(listOf()))

        // when
        val testObserver = repository.fetch().test()

        // then
        testObserver.assertValue( Repository.Result.TimedOut)
        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server timed out and there was data in the cache, when fetching data, the cached data will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.error(TimeoutException()))
        whenever(albumDao.getAlbumsSorted()).thenReturn(Single.just(DATABASE_ALBUMS))

        // when
        val testObserver = repository.fetch().test()

        // then
        testObserver.assertValue( Repository.Result.Success(DOMAIN_ALBUMS, fromCache = true))
        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server was not reachable, when fetching data, the no network will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.error(IOException()))

        // when
        val testObserver = repository.fetch().test()

        // then
        testObserver.assertValue(Repository.Result.NoNetwork)
        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server throws and error, when fetching data, the no network will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenReturn(Single.error(IllegalArgumentException()))

        // when
        val testObserver = repository.fetch().test()

        // then
        testObserver
            .assertError(IllegalArgumentException::class.java)
            .assertNotComplete()
            .assertNoValues()

        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    companion object {
        val NETWORK_ALBUMS = listOf(
            NetworkAlbum(
                userId = 0,
                id = 0,
                title = "Album 1"
            ),
            NetworkAlbum(
                userId = 0,
                id = 1,
                title = "Album 2"
            )
        )

        val DATABASE_ALBUMS = listOf(
            DatabaseAlbum(
                userId = 0,
                id = 0,
                title = "Album 1"
            ),
            DatabaseAlbum(
                userId = 0,
                id = 1,
                title = "Album 2"
            )
        )

        val DATABASE_ALBUMS_USER1 = listOf(
            DatabaseAlbum(
                userId = 1,
                id = 0,
                title = "Album 1"
            ),
            DatabaseAlbum(
                userId = 1,
                id = 1,
                title = "Album 2"
            )
        )

        val DATABASE_ALBUMS_USER2 = listOf(
            DatabaseAlbum(
                userId = 2,
                id = 0,
                title = "Album 1"
            ),
            DatabaseAlbum(
                userId = 2,
                id = 1,
                title = "Album 2"
            )
        )

        val DOMAIN_ALBUMS = listOf(
            Album(
                userId = 0,
                id = 0,
                title = "Album 1"
            ),
            Album(
                userId = 0,
                id = 1,
                title = "Album 2"
            )
        )
        val DOMAIN_ALBUMS_USER1 = listOf(
            Album(
                userId = 1,
                id = 0,
                title = "Album 1"
            ),
            Album(
                userId = 1,
                id = 1,
                title = "Album 2"
            )
        )

        val DOMAIN_ALBUMS_USER2 = listOf(
            Album(
                userId = 2,
                id = 0,
                title = "Album 1"
            ),
            Album(
                userId = 2,
                id = 1,
                title = "Album 2"
            )
        )
    }
}