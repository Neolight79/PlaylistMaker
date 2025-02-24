package com.example.playlistmaker.media.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.bundle.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.media.domain.models.Playlist
import com.example.playlistmaker.media.ui.view_model.PlaylistViewModel
import com.example.playlistmaker.player.ui.fragment.PlayerFragment
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.util.debounce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistFragment : Fragment() {

    // Константы
    companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
        const val PLAYLIST_ID = "PLAYLIST_ID"
    }

    // Инициализируем переменные
    // Для ViewBinding
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    // Для BottomSheet листов
    private lateinit var bottomSheetBehaviorTracks: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetBehaviorMenu: BottomSheetBehavior<LinearLayout>

    // Для функции защиты от многократных нажатий
    private lateinit var onTrackClickDebounce: (Track) -> Unit

    // Для ViewModel
    private val viewModel by viewModel<PlaylistViewModel> {
        parametersOf(
            requireArguments().getInt(
                PLAYLIST_ID, 0
            )
        )
    }

    // Для реализации интерфейса нажатий на элементы списка треков
    private val trackClickListener =
        object : TracksListAdapter.TrackClickListener {
            // Подключаем обработчик нажатия на элемент списка RecyclerView для списка треков в плейлисте
            override fun onTrackClick(track: Track) {
                onTrackClickDebounce(track)
            }
            // Подключаем обработчик долгого нажатия на элемент списка RecyclerView для списка треков в плейлисте
            override fun onTrackLongClick(track: Track) {
                MaterialAlertDialogBuilder(requireContext(),R.style.DialogStyle)
                    .setTitle(R.string.track_delete_dialog_title)
                    .setMessage(R.string.track_delete_dialog_message)
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        // Ничего не делаем
                    }.setPositiveButton(R.string.delete) { _, _ ->
                        // Вызываем процедуру удаления трека
                        viewModel.deleteTrack(track)
                    }.show()
            }
        }

    // Для адаптера RecyclerView треков в плейлисте
    private val tracksListAdapter = TracksListAdapter(trackClickListener)

    // Переменная текущего идентификатора плейлиста для навигации на фрагмент его редактирования
    private lateinit var currentPlaylist: Playlist

    // Описываем методы
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем функцию защиты от повторных нажатий
        onTrackClickDebounce = debounce(CLICK_DEBOUNCE_DELAY, viewLifecycleOwner.lifecycleScope, false) { track ->
            findNavController().navigate(R.id.action_playlistFragment_to_playerFragment, bundleOf(
                PlayerFragment.TRACK_ID to track.trackId)
            )
        }

        // Подключаем BottomSheet
        bottomSheetBehaviorTracks = BottomSheetBehavior.from(binding.standardBottomSheet)
        bottomSheetBehaviorMenu = BottomSheetBehavior.from(binding.menuBottomSheet)

        // Подписываемся на получение объекта с данными LiveData (данные плейлиста)
        viewModel.observePlaylist().observe(viewLifecycleOwner) { playlistData ->
            currentPlaylist = playlistData.first
            renderPlaylistData(playlistData)
        }

        // Подписываемся на получение интента для отправки плейлиста
        viewModel.observeStateIntent().observe(viewLifecycleOwner) {
            startActivity(it)
        }

        // Подписываемся на получение сообщения для одноразового показа
        viewModel.observeStateMessage().observe(viewLifecycleOwner) {
            // Показываем в первую очередь сообщение
            Toast.makeText(requireContext(), it.first, Toast.LENGTH_LONG).show()
            // Если передан флаг закрытия фрагмента, то выходим
            if (it.second) findNavController().navigateUp()
        }

        // Готовим RecyclerView для списка избранных треков
        binding.playlistRecyclerView.adapter = tracksListAdapter

        // Подключаем обработчик нажатия на системную кнопку назад
        activity?.onBackPressedDispatcher?.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        // Подключаем обработчик нажатия на кнопку назад
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Подключаем обработчик нажатия на кнопку "Поделиться"
        binding.playlistShare.setOnClickListener {
            viewModel.sharePlaylist()
        }

        // Подключаем обработчик нажатия на кнопку меню
        binding.playlistMenu.setOnClickListener {
            binding.menuBottomSheet.isVisible = true
            binding.overlay.isVisible = true
            bottomSheetBehaviorMenu.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Обработчики нажатий на пункты всплывающего меню
        // Подключаем обработчик нажатия на кнопку "Поделиться" в меню
        binding.playlistShareMenu.setOnClickListener {
            viewModel.sharePlaylist()
        }

        // Подключаем обработчик нажатия на кнопку "Редактировать плейлист" в меню
        binding.playlistEditMenu.setOnClickListener {
            findNavController().navigate(R.id.action_playlistFragment_to_editPlaylistFragment, bundleOf(
                EditPlaylistFragment.PLAYLIST_ID to currentPlaylist.playlistId)
            )
        }

        // Подключаем обработчик нажатия на кнопку "Удалить плейлист" в меню
        binding.playlistDeleteMenu.setOnClickListener {
            bottomSheetBehaviorMenu.state = BottomSheetBehavior.STATE_HIDDEN
            MaterialAlertDialogBuilder(requireContext(),R.style.DialogStyle)
                .setTitle(R.string.playlist_delete_dialog_title)
                .setMessage(R.string.playlist_delete_dialog_message)
                .setNegativeButton(R.string.no) { _, _ ->
                    // Ничего не делаем
                }.setPositiveButton(R.string.yes) { _, _ ->
                    // Вызываем процедуру удаления плейлиста
                    viewModel.deletePlaylist()
                }.show()
        }

        // Готовим RecyclerView для списка плейлистов
        binding.playlistRecyclerView.adapter = tracksListAdapter

        // Добавляем обработчик событий BottomSheet дополнительных функций
        bottomSheetBehaviorMenu.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.overlay.isVisible = false
                    binding.menuBottomSheet.isVisible = false
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = (slideOffset + 1) / 2
            }
        })

        // Определяем позицию для свернутого BottomSheet по view-болванке на фрагменте
        binding.bottomSpace.doOnLayout {
            if (binding.bottomSpace.height > 0)
                bottomSheetBehaviorTracks.peekHeight = binding.bottomSpace.height
        }

    }

    override fun onResume() {
        super.onResume()
        // Будем обновлять данные и список треков в плейлисте каждый раз при возвращении на фрагмент со списком
        viewModel.loadPlaylistData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Функция отображения данных плейлиста
    @SuppressLint("NotifyDataSetChanged")
    private fun renderPlaylistData(playlistData: Pair<Playlist, List<Track>>) {
        // Наполняем фрагмент данными плейлиста
        // Выводим обложку
        Glide.with(this)
            .load(playlistData.first.playlistImagePath)
            .transform(
                CenterCrop(),
            ).placeholder(R.drawable.placeholder)
            .into(binding.playlistImage)

        // Выводим надписи
        binding.playlistTitle.text = playlistData.first.playlistName
        binding.playlistDescription.text = playlistData.first.playlistDescription
        // Выводим статистику
        val tracksDuration = playlistData.first.playlistTracksDuration / 60000
        binding.playlistDuration.text =
            when {
                tracksDuration % 10 == 1 && tracksDuration % 100 != 11 ->
                    getString(R.string.minutes_quantity_1, tracksDuration)
                tracksDuration % 10 in 2..4 && tracksDuration % 100 !in 12..14 ->
                    getString(R.string.minutes_quantity_2, tracksDuration)
                else ->
                    getString(R.string.minutes_quantity, tracksDuration)
            }
        binding.playlistTracksCount.text =
            when {
                playlistData.first.playlistTracksQuantity % 10 == 1 && playlistData.first.playlistTracksQuantity % 100 != 11 ->
                    getString(R.string.tracks_quantity_1, playlistData.first.playlistTracksQuantity)
                playlistData.first.playlistTracksQuantity % 10 in 2..4 && playlistData.first.playlistTracksQuantity % 100 !in 12..14 ->
                    getString(R.string.tracks_quantity_2, playlistData.first.playlistTracksQuantity)
                else ->
                    getString(R.string.tracks_quantity, playlistData.first.playlistTracksQuantity)
            }

        // Отображаем треки плейлиста
        binding.playlistEmptyMessage.isVisible = playlistData.second.isEmpty()
        tracksListAdapter.tracksList.clear()
        tracksListAdapter.tracksList.addAll(playlistData.second)
        tracksListAdapter.notifyDataSetChanged()

        // Заполняем карточку на контекстном меню
        binding.playlistItem.playlistItemName.text = playlistData.first.playlistName
        binding.playlistItem.playlistItemTracksQuantity.text =
            when {
                playlistData.first.playlistTracksQuantity % 10 == 1 && playlistData.first.playlistTracksQuantity % 100 != 11 ->
                    resources.getString(R.string.tracks_quantity_1, playlistData.first.playlistTracksQuantity)
                playlistData.first.playlistTracksQuantity % 10 in 2..4 && playlistData.first.playlistTracksQuantity % 100 !in 12..14 ->
                    resources.getString(R.string.tracks_quantity_2, playlistData.first.playlistTracksQuantity)
                else ->
                    resources.getString(R.string.tracks_quantity, playlistData.first.playlistTracksQuantity)
            }
        Glide.with(this)
            .load(playlistData.first.playlistImagePath)
            .transform(
                CenterCrop(), RoundedCorners(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        2F, resources.displayMetrics).toInt())
            )
            .placeholder(R.drawable.placeholder)
            .into(binding.playlistItem.playlistItemImage)
    }

}