package com.example.playlistmaker.media.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_table")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // Используем PK с инкрементом
    val playlistName: String,           // Наименование плейлиста
    val playlistDescription: String,    // Описание плейлиста
    val playlistImagePath: String,      // Путь к файлу изображения обложки
    val tracksList: String,             // JSON строка с перечнем идентификаторов добавленных треков
    val tracksQuantity: Int = 0,        // Количество треков в плейлисте
    val tracksDuration: Int = 0         // Продолжительность треков плейлиста в минутах
)