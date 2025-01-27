package com.example.playlistmaker.media.ui.fragment

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.LinearPlaylistItemBinding
import com.example.playlistmaker.media.domain.models.Playlist

class BottomSheetPlaylistsViewHolder(
    private val binding: LinearPlaylistItemBinding,
    private val onPlaylistClick: (Playlist) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {

        itemView.setOnClickListener { onPlaylistClick(item) }

        with (item) {
            binding.playlistName.text = playlistName
            binding.playlistTracksQuantity.text =
                when {
                    playlistTracksQuantity % 10 == 1 && playlistTracksQuantity % 100 != 11 ->
                        itemView.context.getString(R.string.tracks_quantity_1, playlistTracksQuantity)
                    playlistTracksQuantity % 10 in 2..4 && playlistTracksQuantity % 100 !in 12..14 ->
                        itemView.context.getString(R.string.tracks_quantity_2, playlistTracksQuantity)
                    else ->
                        itemView.context.getString(R.string.tracks_quantity, playlistTracksQuantity)
                }
            Glide.with(itemView)
                .load(playlistImagePath)
                .transform(
                    CenterCrop(), RoundedCorners(
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            2F, itemView.resources.displayMetrics).toInt())
                )
                .placeholder(R.drawable.placeholder)
                .into(binding.playlistImage)
        }

    }

}