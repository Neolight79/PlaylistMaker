package com.example.playlistmaker.media.domain.models

data class Playlist(
    val playlistId: Int = 0,                        // Идентификатор плейлиста в базе данных
    var playlistName: String = "",                  // Название плейлиста
    var playlistDescription: String = "",           // Описание плейлиста
    var playlistImagePath: String = "",             // Путь к файлу обложки во внутреннем хранилище
    val playlistTracks: List<Int> = listOf<Int>(),  // Список треков в плейлисте
    val playlistTracksQuantity: Int = 0,            // Количество треков в плейлисте
    val playlistTracksDuration: Int = 0             // Общая продолжительность треков в плейлисте
)