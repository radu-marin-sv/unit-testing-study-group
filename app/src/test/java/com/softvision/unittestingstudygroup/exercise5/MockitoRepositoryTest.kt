package com.softvision.unittestingstudygroup.exercise5

import com.nhaarman.mockitokotlin2.*
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
import retrofit2.Call
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
        val callable: Call<List<NetworkAlbum>> = mock()
        whenever(callable.execute()).thenReturn(Response.success(NETWORK_ALBUMS))
        whenever(albumRestApi.fetchAlbums()).thenReturn(callable)

        repository.fetchOnlyFirst()

        val fetchFirstOrder = inOrder(albumDao, albumRestApi)
        fetchFirstOrder.verify(albumDao).getAlbumsSorted()
        fetchFirstOrder.verify(albumRestApi).fetchAlbums()
        fetchFirstOrder.verify(albumDao).insertAlbums(anyList())
    }

    @Test
    fun `given, when, then`() {
        // given
        whenever(albumDao.getAlbumByUserId(any()))
            .thenReturn(DATABASE_ALBUMS, DATABASE_ALBUMS_USER1, DATABASE_ALBUMS_USER2)

        // when
        val result = repository.fetchAlbumsByUserIdList(listOf(0, 1, 2))

        // then
        val albumList =listOf(DOMAIN_ALBUMS, DOMAIN_ALBUMS_USER1, DOMAIN_ALBUMS_USER2).flatten()
        assertThat(result, equalTo(Repository.Result.Success(albumList, true)))
    }

    @Test
    fun `given the server replied with data, when fetching data, the the data will be successfully fetched`() {
        // given
        val callable: Call<List<NetworkAlbum>> = mock()
        whenever(callable.execute()).thenReturn(Response.success(NETWORK_ALBUMS))
        whenever(albumRestApi.fetchAlbums()).thenReturn(callable)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = false)
        ))

        val captorAlbum = argumentCaptor<List<DatabaseAlbum>>()
        verify(albumDao).insertAlbums(captorAlbum.capture())
        assertThat(captorAlbum.firstValue, equalTo(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server replied with an invalid status code, when fetching data, the the status code will be reported`() {
        // given
        val callable: Call<List<NetworkAlbum>> = mock()
        whenever(callable.execute()).thenReturn(Response.error(404, ResponseBody.create(MediaType.parse(""), "")))
        whenever(albumRestApi.fetchAlbums()).thenReturn(callable)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Invalid(404)
        ))

        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server timed out and there was not data in the cache, when fetching data, the the time out will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenThrow(TimeoutException())
        whenever(albumDao.getAlbumsSorted()).thenReturn(listOf())

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.TimedOut
        ))
        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server timed out and there was data in the cache, when fetching data, the cached data will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenThrow(TimeoutException())
        whenever(albumDao.getAlbumsSorted()).thenReturn(DATABASE_ALBUMS)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = true)
        ))
        verify(albumDao, never()).insertAlbums(ArgumentMatchers.anyList())
    }

    @Test
    fun `given the server was not reachable, when fetching data, the no network will be reported`() {
        // given
        whenever(albumRestApi.fetchAlbums()).thenThrow(IOException())

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.NoNetwork
        ))
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