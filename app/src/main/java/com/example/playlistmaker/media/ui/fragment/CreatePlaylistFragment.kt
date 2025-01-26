package com.example.playlistmaker.media.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.media.domain.models.CreatePlaylistState
import com.example.playlistmaker.media.ui.view_model.CreatePlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class CreatePlaylistFragment : Fragment() {

    // Переменные для ViewBinding
    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    // Переменная для диалога предупреждения о не сохранённых данных
    private lateinit var confirmDialog: MaterialAlertDialogBuilder

    // Переменная для ViewModel
    private val viewModel by viewModel<CreatePlaylistViewModel>()

    // Переменная для признака наличия не сохранённых данных
    private var isFilled = false

    // Переменная для хранения и сравнения URI изображения обложки
    private var playlistImagePath: String = ""

    // Объявляем переменные для отслеживания ввода
    private lateinit var titleTextWatcher: TextWatcher
    private lateinit var descriptionTextWatcher: TextWatcher

    // Методы
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация предупреждения о выходе без сохранения данных
        confirmDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.playlist_creation_cancelling_confirmation)
            .setMessage(R.string.filled_data_lost_warning)
            .setNegativeButton(R.string.cancel) { _, _ ->
                // Ничего не делаем
            }.setPositiveButton(R.string.finish) { _, _ ->
                // Возвращаемся на предыдущий экран
                findNavController().navigateUp()
            }

        // Обработчик нажатия на кнопку Назад
        activity?.onBackPressedDispatcher?.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                exitFragment()
            }
        })

        // Объявление переменной для выбора изображения
        val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.setImageUri(uri)
            }
        }

        // Подписываемся на изменения состояний
        viewModel.observeState().observe(viewLifecycleOwner) {
           render(it)
        }

        // Подписываемся на получение сигнала завершения сохранения с текстом для сообщения
        viewModel.observeFinish().observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }

        // Подключаем обработчик нажатия на кнопку назад
        binding.backButton.setOnClickListener {
            exitFragment()
        }

        // Подключаем запуск выбора изображения для обложки
        binding.addImageButton.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Подключаем обработчик на кнопку сохранения плейлиста
        binding.createPlaylistButton.setOnClickListener {
            viewModel.saveNewPlaylist()
        }

        // Подключаем слежение за вводом текста
        titleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.titleChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        titleTextWatcher.let { binding.playlistName.addTextChangedListener(it) }

        descriptionTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.descriptionChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        descriptionTextWatcher.let { binding.playlistDescription.addTextChangedListener(it) }

    }

    // Обработчик изменений LiveData
    private fun render(state: CreatePlaylistState) {

        // Обновляем значения флагов
        binding.createPlaylistButton.isEnabled = state.isSavable
        isFilled = state.isFilled

        // Заполняем значение для изображения обложки, если требуется
        if (state.playlistImagePath != playlistImagePath) {
            binding.addImageButton.imageTintList = null
            playlistImagePath = state.playlistImagePath
            Glide.with(this)
                .load(File(playlistImagePath))
                .transform(
                    CenterCrop(), RoundedCorners(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8F, this.resources.displayMetrics).toInt())
                )
                .into(binding.addImageButton)
        }

        // Заполняем значение для поля ввода названия, если требуется
        if (binding.playlistName.text.toString() != state.playlistTitle) {
            binding.playlistName.setText(state.playlistTitle)
        }

        // Заполняем значение для поля ввода описания, если требуется
        if (binding.playlistDescription.text.toString() != state.playlistDescription) {
            binding.playlistDescription.setText(state.playlistDescription)
        }
    }

    private fun exitFragment() {
        if (isFilled) {
            confirmDialog.show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}