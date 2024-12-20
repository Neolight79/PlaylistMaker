package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.ItunesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("/search?entity=song&country=RU")
    suspend fun getTracks(
        @Query("term") query: String): ItunesResponse

    @GET("/ru/lookup?")
    suspend fun getTrackData(
        @Query("id") trackId: Int): ItunesResponse

}