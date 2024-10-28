package com.example.playlistmaker.search.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.playlistmaker.search.data.NetworkClient
import com.example.playlistmaker.search.data.dto.ItunesRequest
import com.example.playlistmaker.search.data.dto.ItunesRequest.GetTracks
import com.example.playlistmaker.search.data.dto.ItunesRequest.GetTrackData
import com.example.playlistmaker.search.data.dto.Response

class RetrofitNetworkClient(private val itunesService: ItunesApi, private val context: Context) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (!isConnected()) {
            return Response().apply { resultCode = -1 }
        }
        if (dto !is ItunesRequest) {
            return Response().apply { resultCode = 400 }
        }

        val response = when (dto) {
            is GetTracks -> itunesService.getTracks(dto.expression).execute()
            is GetTrackData -> itunesService.getTrackData(dto.trackId).execute()
        }

        val body = response.body()
        return body?.apply { resultCode = response.code() } ?: Response().apply { resultCode = response.code() }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
        return false
    }
}