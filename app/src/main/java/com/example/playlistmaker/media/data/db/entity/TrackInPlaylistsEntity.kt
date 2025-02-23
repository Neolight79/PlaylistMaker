package com.example.playlistmaker.media.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_in_playlists_table")
data class TrackInPlaylistsEntity(
    @PrimaryKey
    val trackId: Int,           // Идентификатор трека
    val trackName: String,      // Название композиции
    val artistName: String,     // Имя исполнителя
    val collectionName: String?,  // Название альбома
    val releaseDate: String?,    // Год выпуска трека
    val primaryGenreName: String?,    // Жанр музыки
    val country: String?,        // Страна создания
    val trackTimeString: String,   // Продолжительность трека в формате "mm:ss"
    val artworkUrl100: String,   // Ссылка на изображение обложки малого масштаба
    val artworkUrl512: String,   // Ссылка на изображение обложки крупного масштаба
    val previewUrl: String,      // Ссылка на 30ти секундный фрагмент
    val trackTimeMillis: Int     // Продолжительность трека в миллисекундах
)