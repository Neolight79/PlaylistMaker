package com.example.playlistmaker.media.data.converters

import com.example.playlistmaker.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.media.domain.models.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbConvertor {

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.playlistId,
            playlistName = playlist.playlistName,
            playlistDescription = playlist.playlistDescription,
            playlistImagePath = playlist.playlistImagePath,
            tracksList = Gson().toJson(playlist.playlistTracks),
            tracksQuantity = playlist.playlistTracksQuantity)
    }

    fun map(playlist: PlaylistEntity): Playlist {
        return Playlist(
            playlistId = playlist.id,
            playlistName = playlist.playlistName,
            playlistDescription = playlist.playlistDescription,
            playlistImagePath = playlist.playlistImagePath,
            playlistTracks = Gson().fromJson(playlist.tracksList, object : TypeToken<List<Int>>() {}.type),
            playlistTracksQuantity = playlist.tracksQuantity)
    }

}