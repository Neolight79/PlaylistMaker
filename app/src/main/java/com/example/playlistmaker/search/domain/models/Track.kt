package com.example.playlistmaker.search.domain.models

data class Track(
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
    val previewUrl: String      // Ссылка на 30ти секундный фрагмент
)