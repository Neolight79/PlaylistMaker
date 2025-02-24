package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // Создание нового плейлиста
    @Insert(entity = PlaylistEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    // Получение полного списка плейлистов со всеми полями
    @Query("SELECT * FROM playlist_table")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    // Получение данных плейлиста
    @Query("SELECT * FROM playlist_table WHERE id = :playlistId")
    fun getPlaylist(playlistId: Int): Flow<PlaylistEntity>

    // Удаление плейлиста
    @Query("DELETE FROM playlist_table WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

}