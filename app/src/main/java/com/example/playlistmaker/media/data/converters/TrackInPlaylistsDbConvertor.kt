package com.example.playlistmaker.media.data.converters

import com.example.playlistmaker.media.data.db.entity.TrackInPlaylistsEntity
import com.example.playlistmaker.search.domain.models.Track

class TrackInPlaylistsDbConvertor {

    fun map(track: Track): TrackInPlaylistsEntity {
        return TrackInPlaylistsEntity(
            track.trackId,
            track.trackName,
            track.artistName,
            track.collectionName,
            track.releaseDate,
            track.primaryGenreName,
            track.country,
            track.trackTimeString,
            track.artworkUrl100,
            track.artworkUrl512,
            track.previewUrl,
            track.trackTimeMillis)
    }

    fun map(track: TrackInPlaylistsEntity): Track {
        return Track(
            track.trackId,
            track.trackName,
            track.artistName,
            track.collectionName,
            track.releaseDate,
            track.primaryGenreName,
            track.country,
            track.trackTimeString,
            track.artworkUrl100,
            track.artworkUrl512,
            track.previewUrl,
            track.trackTimeMillis)
    }

}