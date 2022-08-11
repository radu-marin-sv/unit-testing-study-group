package com.softvision.unittestingstudygroup.exercise5

import android.content.Context
import androidx.lifecycle.*
import com.softvision.unittestingstudygroup.R
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MyViewModel(
    private val repository: Repository,
) : ViewModel() {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>>
        get() = _albums

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing

    private val _error = MutableLiveData<Int>()
    val error: LiveData<Int>
        get() = _error

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.postValue(true)
            when (val result = repository.fetch()) {
                is Repository.Result.Success -> handleSuccess(result.albums, result.fromCache)
                Repository.Result.TimedOut -> handleTimeout()
                is Repository.Result.Invalid -> handleInvalid(result.statusCode)
                Repository.Result.NoNetwork -> handleTimeout()
            }
            _isRefreshing.postValue(false)
        }
    }

    private fun handleSuccess(albums: List<Album>, fromCache: Boolean) {
        _albums.postValue(albums)
        if (fromCache) {
            _error.postValue(R.string.cached_data)
        }
    }

    private fun handleTimeout() {
        _error.postValue(R.string.no_internet)
    }

    private fun handleInvalid(statusCode: Int) {
        when (statusCode) {
            in 400..499 -> _error.postValue(R.string.client_problem)
            in 500..599 -> _error.postValue(R.string.server_problem)
            else -> _error.postValue( R.string.unknown_problem)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val repository = RepositoryFactory.create(context)
            return MyViewModel(repository) as T
        }

    }
}