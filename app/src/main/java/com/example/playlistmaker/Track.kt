package com.example.playlistmaker

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

data class Track(
    val trackId: Int,           // Идентификатор трека
    val trackName: String,      // Название композиции
    val artistName: String,     // Имя исполнителя
    val collectionName: String?,  // Название альбома
    val releaseDate: String?,    // Год выпуска трека
    val primaryGenreName: String?,    // Жанр музыки
    val country: String?,        // Страна создания
    val trackTimeMillis: Int,   // Продолжительность трека в миллисекундах
    val artworkUrl100: String   // Ссылка на изображение обложки
): Serializable {
    val artworkUrl512 get() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
    val trackTimeString get() = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
}
