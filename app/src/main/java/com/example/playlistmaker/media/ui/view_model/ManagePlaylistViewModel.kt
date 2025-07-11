package com.example.playlistmaker.media.ui.view_model

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.ManagePlaylistState
import com.example.playlistmaker.media.domain.models.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ManagePlaylistViewModel(
    private val playlistId: Int,
    private val playlistsInteractor: PlaylistsInteractor,
    private val application: Application): ViewModel() {

    companion object {
        const val IMAGES_DIRECTORY = "playlistImages"
        const val UNKNOWN_FILENAME = "Unknown"
    }

    // Переменная для хранения текущего плейлиста
    private lateinit var currentPlaylist: Playlist

    // StateFlow для состояния экрана создания/изменения плейлиста
    private val _managePlaylistState = MutableStateFlow<ManagePlaylistState>(ManagePlaylistState())
    val managePlaylistState: StateFlow<ManagePlaylistState> = _managePlaylistState.asStateFlow()

    // StateFlow для строки сообщения перед закрытием окна создания/изменения плейлиста
    private val _finishManagePlaylistState = MutableStateFlow("")
    val finishManagePlaylistState: StateFlow<String> = _finishManagePlaylistState.asStateFlow()

    init {
        loadPlaylistData()
    }

    // Метод для переноса изображения во внутреннее хранилище
    fun setImageUri(uri: Uri) {

        // Переменная для Context
        val context = application.applicationContext

        // Создаём экземпляр класса File, который указывает на нужный каталог
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_DIRECTORY)

        // Создаём каталог, если он не создан
        if (!filePath.exists()){
            filePath.mkdirs()
        }

        // Создаём экземпляр класса File, который указывает на файл внутри каталога
        val fileName = getFileNameFromUri(uri, context)
        var file = File(filePath, "$fileName.jpg")
        var i = 1
        while (file.exists()) {
            file = File(filePath, "$fileName-($i).jpg")
            i++
        }

        // Создаём входящий поток байтов из выбранной картинки
        val inputStream = context.contentResolver?.openInputStream(uri)

        // Создаём исходящий поток байтов в созданный выше файл
        val outputStream = FileOutputStream(file)

        // Записываем картинку с помощью BitmapFactory
        BitmapFactory
            .decodeStream(inputStream)
            .compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

        // Сохраняем путь к записанному файлу в переменную хранения картинки плейлиста
        currentPlaylist.playlistImagePath = file.toString()
        // Формируем и отправляем изменения во фрагмент
        renderState()
    }

    fun titleChanged(title: String) {
        if (title != currentPlaylist.playlistName) {
            currentPlaylist.playlistName = title
            renderState()
        }
    }

    fun descriptionChanged(description: String) {
        if (description != currentPlaylist.playlistDescription) {
            currentPlaylist.playlistDescription = description
            renderState()
        }
    }

    // Функция получения данных плейлиста
    private fun loadPlaylistData() {
        when (playlistId == 0) {
            // Если playlistId в нуле, значит мы создаём новый плейлист
            true -> {
                currentPlaylist = Playlist(
                    playlistId = playlistId,
                    playlistName = "",
                    playlistDescription = "",
                    playlistImagePath = "",
                    playlistTracks = listOf(),
                    playlistTracksQuantity = 0,
                    playlistTracksDuration = 0
                )
                renderState()
            }
            // Если playlistId не нулевой, значит мы загружаем из базы данных его содержимое перед редактированием
            false -> {
                viewModelScope.launch {
                    playlistsInteractor.getPlaylistData(playlistId).collect { playlistData ->
                        currentPlaylist = playlistData.first
                        renderState()
                    }
                }
            }
        }
    }

    fun savePlaylistData() {
        viewModelScope.launch {
            playlistsInteractor.createPlaylist(currentPlaylist)
        }

        // Передаём сообщение для вывода при закрытии формы создания/изменения плейлиста
        _finishManagePlaylistState.value = when (playlistId == 0) {
            true -> application.getString(R.string.playlist_created_message, currentPlaylist.playlistName)
            false -> application.getString(R.string.playlist_updated_message, currentPlaylist.playlistName)
        }
    }

    fun renderState() {
        _managePlaylistState.value =
            ManagePlaylistState(
                isSavable = currentPlaylist.playlistName.isNotEmpty(),
                isFilled = currentPlaylist.playlistImagePath.isNotEmpty() || currentPlaylist.playlistName.isNotEmpty() || currentPlaylist.playlistDescription.isNotEmpty(),
                isNewPlaylist = (playlistId == 0),
                playlistImagePath = currentPlaylist.playlistImagePath,
                playlistTitle = currentPlaylist.playlistName,
                playlistDescription = currentPlaylist.playlistDescription)
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String {
        var fileName = UNKNOWN_FILENAME
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName.substringBeforeLast(".")
    }

}