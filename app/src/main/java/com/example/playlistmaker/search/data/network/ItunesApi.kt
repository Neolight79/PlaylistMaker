package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.ItunesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("/search?entity=song&country=RU")
    fun getTracks(
        @Query("term") query: String): Call<ItunesResponse>

    @GET("/ru/lookup?")
    fun getTrackData(
        @Query("id") trackId: Int): Call<ItunesResponse>

}