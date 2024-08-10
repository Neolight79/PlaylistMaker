package com.example.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("/search")
    fun getTracks(
        @Query("entity") entity: String,
        @Query("country") country: String,
        @Query("term") query: String): Call<ItunesResponse>

}