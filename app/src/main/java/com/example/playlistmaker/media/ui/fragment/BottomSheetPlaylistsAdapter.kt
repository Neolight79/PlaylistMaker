package com.example.playlistmaker.media.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.LinearPlaylistItemBinding
import com.example.playlistmaker.media.domain.models.Playlist

class BottomSheetPlaylistsAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistsViewHolder> () {

    val playlists = mutableListOf<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetPlaylistsViewHolder =
        BottomSheetPlaylistsViewHolder(LinearPlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), onPlaylistClick)

    override fun onBindViewHolder(holder: BottomSheetPlaylistsViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item
    }

}