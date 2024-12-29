package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    // Добавление трека в избранное
    @Insert(entity = TrackEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    // Удаление трека из избранного
    @Query("DELETE FROM TRACK_TABLE WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Int)

    // Получение полного списка избранных треков со всеми полями
    @Query("SELECT * FROM track_table")
    fun getTracks(): Flow<List<TrackEntity>>

    // Получение полного списка идентификаторов избранных треков
    @Query("SELECT trackId FROM track_table")
    suspend fun getTrackIds(): List<Int>

}