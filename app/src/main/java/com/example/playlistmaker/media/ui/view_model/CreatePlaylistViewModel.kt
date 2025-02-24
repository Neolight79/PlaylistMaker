package com.example.playlistmaker.media.ui.view_model

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.media.domain.db.PlaylistsInteractor
import com.example.playlistmaker.media.domain.models.CreatePlaylistState
import com.example.playlistmaker.media.domain.models.Playlist
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

open class CreatePlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val application: Application): ViewModel() {

    companion object {
        const val IMAGES_DIRECTORY = "playlistImages"
        const val UNKNOWN_FILENAME = "Unknown"
    }

    // Переменные для хранения данных
    var playlistImagePath: String = ""
    var playlistTitle: String = ""
    var playlistDescription: String = ""

    // LiveData для состояний формы создания плейлиста
    private val stateLiveData = MutableLiveData<CreatePlaylistState>()
    fun observeState(): LiveData<CreatePlaylistState> = stateLiveData

    // LiveData для сообщения при закрытии фрагмента
    val finishLiveData = MutableLiveData<String>()
    fun observeFinish(): LiveData<String> = finishLiveData

    init {
        // Инициализируем фрагмент исходными значениями
        renderState()
    }

    // Метод для переноса изображения во внутреннее хранилище
    fun setImageUri(uri: Uri) {

        // Переменная для Context
        val context = application.applicationContext

        // Создаём экземпляр класса File, который указывает на нужный каталог
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), IMAGES_DIRECTORY)

        // Создаем каталог, если он не создан
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
        playlistImagePath = file.toString()
        // Формируем и отправляем изменения во фрагмент
        renderState()
    }

    fun titleChanged(title: String) {
        if (title != playlistTitle) {
            playlistTitle = title
            renderState()
        }
    }

    fun descriptionChanged(description: String) {
        if (description != playlistDescription) {
            playlistDescription = description
            renderState()
        }
    }

    open fun saveNewPlaylist() {
        viewModelScope.launch {
            val playlist = Playlist(
                playlistId = 0,
                playlistName = playlistTitle,
                playlistDescription = playlistDescription,
                playlistImagePath = playlistImagePath,
                playlistTracks = emptyList(),
                playlistTracksQuantity = 0,
                playlistTracksDuration = 0
            )
            playlistsInteractor.createPlaylist(playlist)
        }

        // Передаём сообщение для вывода при закрытии формы создания плейлиста
        finishLiveData.postValue(application.getString(
            R.string.playlist_created_message, playlistTitle))
    }

    fun renderState() {
        stateLiveData.postValue(
            CreatePlaylistState(
                isSavable = playlistTitle.isNotEmpty(),
                isFilled = playlistImagePath.isNotEmpty() || playlistTitle.isNotEmpty() || playlistDescription.isNotEmpty(),
                playlistImagePath = playlistImagePath,
                playlistTitle = playlistTitle,
                playlistDescription = playlistDescription))
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
