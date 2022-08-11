package com.softvision.unittestingstudygroup.exercise5

import com.softvision.unittestingstudygroup.ThreadingExtension
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
class MockkRepositoryTest {

    @MockK
    private lateinit var albumRestApi: AlbumRestApi
    @MockK
    private lateinit var albumDao: AlbumDao

    private lateinit var repository: Repository

    @get:Rule
    val mainCoroutineDispatcher = ThreadingExtension()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repository = Repository(albumRestApi, albumDao, mainCoroutineDispatcher.testDispatcherProvider)
    }

    @Test
    fun `given a server replied with data and we don't have nothing stored, when fetching data, then the fetch methods are called in expected order`() = runBlockingTest {
        coEvery { albumRestApi.fetchAlbums().execute() } returns Response.success(NETWORK_ALBUMS)
        coEvery { albumDao.getAlbumsSorted() } returns listOf()
        coJustRun { albumDao.insertAlbums(any()) }

        repository.fetchOnlyFirst()

        coVerifySequence {
            albumDao.getAlbumsSorted()
            albumRestApi.fetchAlbums()
            albumDao.insertAlbums(any())
        }

        confirmVerified(albumDao, albumRestApi)
    }

    @Test
    fun `given, when, then`() = runBlockingTest {
        // given
        coEvery { albumDao.getAlbumByUserId(any()) } returnsMany listOf(
            DATABASE_ALBUMS, DATABASE_ALBUMS_USER1, DATABASE_ALBUMS_USER2
        )

        // when
        val result = repository.fetchAlbumsByUserIdList(listOf(0, 1, 2))

        // then
        val albumList =listOf(DOMAIN_ALBUMS, DOMAIN_ALBUMS_USER1, DOMAIN_ALBUMS_USER2).flatten()
        assertThat(result, equalTo(Repository.Result.Success(albumList, true)))
    }

    @Test
    fun `given the server replied with data, when fetching data, the the data will be successfully fetched`() = runBlockingTest {
        val albumsSlot = slot<List<DatabaseAlbum>>()

        // given
        coEvery {  albumRestApi.fetchAlbums().execute() } returns Response.success(NETWORK_ALBUMS)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = false)
        ))

        coVerify { albumDao.insertAlbums(capture(albumsSlot)) }
        assertThat(albumsSlot.captured, equalTo(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server replied with an invalid status code, when fetching data, the the status code will be reported`() = runBlockingTest {
        // given
        coEvery { albumRestApi.fetchAlbums().execute() } returns Response.error(404, ResponseBody.create(MediaType.parse(""), ""))

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Invalid(404)
        ))

        coVerify(exactly = 0) { albumDao.insertAlbums(any()) }
    }

    @Test
    fun `given the server timed out and there was not data in the cache, when fetching data, the the time out will be reported`() = runBlockingTest {
        // given
        coEvery { albumRestApi.fetchAlbums() } throws TimeoutException()
        coEvery { albumDao.getAlbumsSorted() } returns listOf()

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.TimedOut
        ))

        coVerify(exactly = 0) { albumDao.insertAlbums(any()) }
    }

    @Test
    fun `given the server timed out and there was data in the cache, when fetching data, the cached data will be reported`() = runBlockingTest {
        // given
        coEvery { albumRestApi.fetchAlbums() } throws TimeoutException()
        coEvery { albumDao.getAlbumsSorted() } returns DATABASE_ALBUMS

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = true)
        ))
        coVerify(exactly = 0) { albumDao.insertAlbums(any()) }
    }

    @Test
    fun `given the server was not reachable, when fetching data, the no network will be reported`() = runBlockingTest {
        // given
        coEvery { albumRestApi.fetchAlbums() } throws IOException()

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.NoNetwork
        ))
        coVerify(exactly = 0) { albumDao.insertAlbums(any()) }
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