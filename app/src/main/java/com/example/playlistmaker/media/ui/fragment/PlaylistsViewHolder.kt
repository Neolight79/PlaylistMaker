package com.example.playlistmaker.media.ui.fragment

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemBinding
import com.example.playlistmaker.media.domain.models.Playlist
import java.io.File

class PlaylistsViewHolder(
    private val binding: PlaylistItemBinding,
    private val clickListener: PlaylistsAdapter.PlaylistClickListener
): RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {

        itemView.setOnClickListener { clickListener.onPlaylistClick(item) }

        with (item) {
            binding.playlistName.text = playlistName
            binding.playlistTracksQuantity.text = itemView.context.getString(R.string.tracks_quantity, playlistTracksQuantity)
            Glide.with(itemView)
                .load(File(playlistImagePath))
                .transform(
                    CenterCrop(), RoundedCorners(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            8F, itemView.resources.displayMetrics).toInt())
                )
                .placeholder(R.drawable.placeholder)
                .into(binding.playlistImage)
        }


    }

}