package com.example.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("/search?entity=song&country=RU")
    fun getTracks(
        @Query("term") query: String): Call<ItunesResponse>

}