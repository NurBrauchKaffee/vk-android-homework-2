package ru.justneedcoffee.apishenanigans.repository

import ru.justneedcoffee.apishenanigans.api.GiphyApi
import ru.justneedcoffee.apishenanigans.models.GiphyData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import java.io.IOException

class GiphyPagingSource(
    private val api: GiphyApi,
    private val apiKey: String
) : PagingSource<Int, GiphyData>() {

    override fun getRefreshKey(state: PagingState<Int, GiphyData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GiphyData> {
        return try {
            val pageIndex = params.key ?: 0
            val limit = params.loadSize
            val offset = pageIndex * limit

            val response = api.getTrendingGifs(
                apiKey = apiKey,
                limit = limit,
                offset = offset
            )

            val gifs = response.data

            LoadResult.Page(
                data = gifs,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (gifs.isEmpty()) null else pageIndex + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}