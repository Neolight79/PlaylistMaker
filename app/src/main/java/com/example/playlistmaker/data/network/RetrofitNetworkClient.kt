package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.NetworkClient
import com.example.playlistmaker.data.dto.ItunesRequest
import com.example.playlistmaker.data.dto.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient : NetworkClient {

    private val itunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ItunesApi::class.java)

    override fun doRequest(dto: Any): Response {
        if (dto is ItunesRequest) {
            val response = itunesService.getTracks(dto.expression).execute()

            val body = response.body() ?: Response()

            return body.apply { resultCode = response.code() }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}