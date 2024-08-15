package com.example.nits

data class SearchResponse(
    val images_results: List<ImageResult>
)

data class ImageResult(
    val image: String
)
