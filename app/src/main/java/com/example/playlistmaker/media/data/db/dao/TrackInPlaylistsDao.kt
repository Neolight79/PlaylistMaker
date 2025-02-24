package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.TrackInPlaylistsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackInPlaylistsDao {

    // Добавление трека в список треков в плейлистах
    @Insert(entity = TrackInPlaylistsEntity::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackInPlaylistsEntity)

    // Получение списка треков с фильтром по списку ID
    @Query("SELECT * FROM track_in_playlists_table WHERE trackId IN (:tracksIds)")
    fun getTracksInPlaylists(tracksIds: List<Int>): Flow<List<TrackInPlaylistsEntity>>

    // Удаление трека из таблицы треков в плейлистах
    @Query("DELETE FROM track_in_playlists_table WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Int)

    // Удаление из списка орфанных треков (которых нет в переданном списке)
    @Query("DELETE FROM track_in_playlists_table WHERE NOT trackId IN (:keepTracksIds)")
    suspend fun cleanUpOrphanedTracks(keepTracksIds: List<Int>)

}