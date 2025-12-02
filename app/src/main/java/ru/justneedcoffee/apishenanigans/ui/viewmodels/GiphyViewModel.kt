package ru.justneedcoffee.apishenanigans.ui.viewmodels

import ru.justneedcoffee.apishenanigans.api.GiphyApi
import ru.justneedcoffee.apishenanigans.models.GiphyData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import ru.justneedcoffee.apishenanigans.BuildConfig
import ru.justneedcoffee.apishenanigans.repository.GiphyPagingSource
import javax.inject.Inject

private const val API_KEY = BuildConfig.API_KEY

@HiltViewModel
class GiphyViewModel @Inject constructor(
    private val api: GiphyApi
) : ViewModel() {
    val gifs: Flow<PagingData<GiphyData>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = { GiphyPagingSource(api, API_KEY) }
    ).flow.cachedIn(viewModelScope)
}