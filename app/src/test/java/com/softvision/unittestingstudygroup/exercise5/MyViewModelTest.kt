package com.softvision.unittestingstudygroup.exercise5

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verifySequence
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock

class MyViewModelTest {
    @MockK
    private lateinit var repository: Repository

    @MockK
    private lateinit var executors: AppExecutors

    @MockK(relaxUnitFun = true)
    private lateinit var albumsObserver: Observer<List<Album>>

    @MockK(relaxUnitFun = true)
    private lateinit var isRefreshingObserver: Observer<Boolean>

    @InjectMockKs
    private lateinit var viewModel: MyViewModel

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { executors.io.execute(any()) } answers { (it.invocation.args[0] as Runnable).run() }
        every { executors.main.execute(any()) } answers { (it.invocation.args[0] as Runnable).run() }

        viewModel.albums.observeForever(albumsObserver)
        viewModel.isRefreshing.observeForever(isRefreshingObserver)
    }

    @After
    fun tearDown() {
        viewModel.albums.removeObserver(albumsObserver)
        viewModel.isRefreshing.removeObserver(isRefreshingObserver)
    }

    @Test
    fun `given we successfully fetched data from the server, when refreshing, then the UI will display the list`() {
        // given
        every { repository.fetch() } returns Repository.Result.Success(
            albums = DOMAIN_ALBUMS,
            fromCache = false
        )

        // when
        viewModel.refresh()

        // then
        verifySequence {
            isRefreshingObserver.onChanged(true)
            albumsObserver.onChanged(DOMAIN_ALBUMS)
            isRefreshingObserver.onChanged(false)
        }
        confirmVerified(albumsObserver, isRefreshingObserver)
    }

    companion object {
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