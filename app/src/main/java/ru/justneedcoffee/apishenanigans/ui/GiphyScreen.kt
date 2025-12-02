package ru.justneedcoffee.apishenanigans.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import ru.justneedcoffee.apishenanigans.R
import ru.justneedcoffee.apishenanigans.models.GiphyData
import ru.justneedcoffee.apishenanigans.ui.viewmodels.GiphyViewModel

fun getImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .diskCache {
            coil.disk.DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizePercent(0.25)
                .build()
        }
        .crossfade(true)
        .build()
}

@Composable
fun GiphyScreen(
    viewModel: GiphyViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val lazyPagingItems = viewModel.gifs.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GiphyList(
                items = lazyPagingItems,
                onItemClick = { index, url ->
                    showNotification(context, index)
                    onNavigateToDetail(url)
                }
            )

            lazyPagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    loadState.refresh is LoadState.Error -> {
                        ErrorView(
                            modifier = Modifier.align(Alignment.Center),
                            onRetry = { retry() }
                        )
                    }
                    loadState.refresh is LoadState.NotLoading && lazyPagingItems.itemCount == 0 -> {

                    }
                }
            }
        }
    }
}

@Composable
fun GiphyList(
    items: LazyPagingItems<GiphyData>,
    onItemClick: (Int, String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        items(count = items.itemCount) { index ->
            items[index]?.let { gif ->
                GifItem(
                    gif = gif,
                    index = index,
                    onClick = { clickedIndex ->
                        onItemClick(clickedIndex, gif.images.fixedWidth.url)
                    }
                )
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            if (items.loadState.append is LoadState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.loadState.append is LoadState.Error) {
                ErrorView(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onRetry = { items.retry() }
                )
            }
        }
    }
}

@Composable
fun GifItem(
    gif: GiphyData,
    index: Int,
    onClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val aspectRatio = gif.images.fixedWidth.width.toFloat() / gif.images.fixedWidth.height.toFloat()

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(index) }
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(gif.images.fixedWidth.url)
                .crossfade(true)
                .build(),
            imageLoader = getImageLoader(context)
        )

        Image(
            painter = painter,
            contentDescription = stringResource(R.string.content_desc_gif),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        )
    }
}

@Composable
fun ErrorView(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.error_loading))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry_button))
        }
    }
}

@Composable
fun DetailScreen(
    url: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable { onBack() },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            imageLoader = getImageLoader(LocalContext.current),
            contentDescription = null,
            loading = { CircularProgressIndicator(color = Color.White) },
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("GIPHY_CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

private fun showNotification(context: Context, index: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    val builder = NotificationCompat.Builder(context, "GIPHY_CHANNEL_ID")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_text, index))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(index, builder.build())
    }
}