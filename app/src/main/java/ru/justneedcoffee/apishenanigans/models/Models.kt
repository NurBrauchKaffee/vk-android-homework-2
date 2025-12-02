package ru.justneedcoffee.apishenanigans.models

import com.google.gson.annotations.SerializedName

data class GiphyResponse(
    @SerializedName("data") val data: List<GiphyData>,
    @SerializedName("meta") val meta: Meta
)

data class GiphyData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("images") val images: Images
)

data class Images(
    @SerializedName("fixed_width") val fixedWidth: ImageInfo
)

data class ImageInfo(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

data class Meta(
    @SerializedName("msg") val msg: String,
    @SerializedName("status") val status: Int
)