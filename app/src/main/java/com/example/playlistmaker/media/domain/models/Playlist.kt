package com.example.playlistmaker.media.domain.models

data class Playlist(
    val playlistId: Int,                 // Идентификатор трека в базе данных
    val playlistName: String,            // Название плейлиста
    val playlistDescription: String,     // Описание плейлиста
    val playlistImagePath: String,        // Путь к файлу обложки во внутреннем хранилище
    val playlistTracks: List<Int>,       // Список треков в плейлисте
    val playlistTracksQuantity: Int     // Количество треков в плейлисте
)