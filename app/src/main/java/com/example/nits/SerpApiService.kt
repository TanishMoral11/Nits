package com.example.nits

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SerpApiService {
    @GET("search")
    fun getImages(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("tbm") type: String
    ): Call<SearchResponse>
}
