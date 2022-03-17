package com.softvision.unittestingstudygroup.exercise5

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

class RepositoryTest {
    private lateinit var repository: Repository

    @Test
    fun `given the server replied with data, when fetching data, the the data will be successfully fetched`() {
        // given
        val dao = AlbumDaoMock()
        repository = Repository(createRestApiStub { Response.success(NETWORK_ALBUMS) }, dao)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = false)
        ))
        dao.verifyCalled("insertAlbums", listOf(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server replied with an invalid status code, when fetching data, the the status code will be reported`() {
        // given
        val dao = AlbumDaoMock()
        repository = Repository(
            createRestApiStub { Response.error(404, ResponseBody.create(MediaType.parse(""), "")) },
            dao
        )

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Invalid(404)
        ))
        dao.verifyNeverCalled("insertAlbums", listOf(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server timed out and there was not data in the cache, when fetching data, the the time out will be reported`() {
        // given
        val dao = AlbumDaoMock()
        dao.doWhenGetAlbumsSortedIsCalled = { listOf() }
        repository = Repository(createRestApiStub { throw TimeoutException() }, dao)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.TimedOut
        ))
        dao.verifyNeverCalled("insertAlbums", listOf(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server timed out and there was data in the cache, when fetching data, the cached data will be reported`() {
        // given
        val dao = AlbumDaoMock()
        dao.doWhenGetAlbumsSortedIsCalled = { DATABASE_ALBUMS }
        repository = Repository(createRestApiStub { throw TimeoutException() }, dao)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.Success(DOMAIN_ALBUMS, fromCache = true)
        ))
        dao.verifyNeverCalled("insertAlbums", listOf(DATABASE_ALBUMS))
    }

    @Test
    fun `given the server was not reachable, when fetching data, the no network will be reported`() {
        // given
        val dao = AlbumDaoMock()
        repository = Repository(createRestApiStub { throw IOException() }, dao)

        // when
        val result = repository.fetch()

        // then
        assertThat(result, equalTo(
            Repository.Result.NoNetwork
        ))
        dao.verifyNeverCalled("insertAlbums", listOf(DATABASE_ALBUMS))
    }

    private fun createRestApiStub(callback: () -> Response<List<NetworkAlbum>>): AlbumRestApi {
        return object : AlbumRestApi {
            override fun fetchAlbums(): Response<List<NetworkAlbum>> {
                return callback()
            }
        }
    }

    data class Invocation(val method: String, val args: List<Any?> = listOf())

    class AlbumDaoMock(private val relaxed: Boolean = false) : AlbumDao {
        private val invocations = mutableListOf<Invocation>()

        var doWhenGetAlbumsSortedIsCalled: () -> List<DatabaseAlbum> = {
            relaxCheck()
            listOf()
        }

        private fun relaxCheck() {
            if (!relaxed) {
                throw NotImplementedError()
            }
        }

        override fun getAlbumsSorted(): List<DatabaseAlbum> {
            invocations.add(Invocation("getAlbumsSorted"))
            return doWhenGetAlbumsSortedIsCalled()
        }

        override fun insertAlbums(albums: List<DatabaseAlbum>) {
            invocations.add(Invocation("insertAlbums", listOf<Any?>(albums)))
        }

        fun verifyCalled(method: String, args: List<Any?> = listOf(), times: Int = 1) {
            val invocation = Invocation(method, args)
            assertThat(invocations.filter { it == invocation }.size, equalTo(times))
        }

        fun verifyNeverCalled(method: String, args: List<Any?> = listOf(), times: Int = 1) = verifyCalled(method, args, 0)
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
    }
}