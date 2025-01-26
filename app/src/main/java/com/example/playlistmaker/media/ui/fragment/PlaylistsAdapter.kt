package com.example.playlistmaker.media.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.media.domain.models.Playlist

class PlaylistsAdapter(
    private val clickListener: PlaylistClickListener
) : RecyclerView.Adapter<PlaylistsViewHolder> () {

    var playlists = mutableListOf<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder =
        PlaylistsViewHolder(PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item
    }

    interface PlaylistClickListener {
        fun onPlaylistClick(playlist: Playlist)
    }
}