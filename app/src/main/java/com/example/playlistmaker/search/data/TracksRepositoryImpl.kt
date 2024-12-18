package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.data.dto.ItunesRequest
import com.example.playlistmaker.search.data.dto.ItunesResponse
import com.example.playlistmaker.search.data.dto.Response
import com.example.playlistmaker.search.domain.api.TracksRepository
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.search.domain.models.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(expression: String): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.doRequest(ItunesRequest.GetTracks(expression))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                emit(mapResults(response))
            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))
            }
        }
    }

    override fun loadTrackData(trackId: Int): Flow<Resource<List<Track>>> = flow {
        val response = networkClient.doRequest(ItunesRequest.GetTrackData(trackId))
        when (response.resultCode) {
            -1 -> {
                emit(Resource.Error("Проверьте подключение к интернету"))
            }
            200 -> {
                emit(mapResults(response))
            }
            else -> {
                emit(Resource.Error("Ошибка сервера"))
            }
        }
    }

    private fun mapResults(response: Response): Resource<List<Track>> {
        return Resource.Success((response as ItunesResponse).results.map {
            Track(
                it.trackId,
                it.trackName,
                it.artistName,
                it.collectionName,
                if (it.releaseDate.isNullOrEmpty()) "" else it.releaseDate.substring(0, 4),
                it.primaryGenreName,
                it.country,
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis),
                it.artworkUrl100,
                it.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"),
                it.previewUrl) })
    }

}