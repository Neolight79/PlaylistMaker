package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.playlistmaker.media.data.db.entity.TrackInPlaylistsEntity

@Dao
interface TrackInPlaylistsDao {

    // Добавление трека в список треков в плейлистах
    @Insert(entity = TrackInPlaylistsEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackInPlaylistsEntity)

}