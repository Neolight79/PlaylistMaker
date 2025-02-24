package com.example.playlistmaker.media.data.converters

import com.example.playlistmaker.media.data.db.entity.TrackEntity
import com.example.playlistmaker.search.domain.models.Track

class TrackDbConvertor {

    fun map(track: Track): TrackEntity {
        return TrackEntity(
            0,
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

    fun map(track: TrackEntity): Track {
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