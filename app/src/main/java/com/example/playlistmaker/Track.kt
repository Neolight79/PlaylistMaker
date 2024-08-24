package com.example.playlistmaker

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
)
