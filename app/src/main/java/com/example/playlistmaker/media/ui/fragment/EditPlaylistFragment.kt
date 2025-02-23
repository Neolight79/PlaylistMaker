package com.example.playlistmaker.media.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.view_model.EditPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : CreatePlaylistFragment() {

    companion object {
        const val PLAYLIST_ID = "PLAYLIST_ID"
    }

    override val viewModel by viewModel<EditPlaylistViewModel> {
        parametersOf(
            requireArguments().getInt(
                PLAYLIST_ID, 0
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Корректируем заголовок и название кнопки
        binding.playlistTitle.text = resources.getString(R.string.edit_playlist_title)
        binding.createPlaylistButton.text = resources.getString(R.string.save)
    }

    override fun exitFragment() {
        findNavController().navigateUp()
    }

}