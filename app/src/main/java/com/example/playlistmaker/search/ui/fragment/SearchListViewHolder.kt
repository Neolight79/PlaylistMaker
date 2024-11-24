package com.example.playlistmaker.search.ui.fragment

import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ListSearchItemBinding
import com.example.playlistmaker.search.domain.models.Track

class SearchListViewHolder(private val binding: ListSearchItemBinding, private val clickListener: SearchListAdapter.TrackClickListener): RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Track) {

        itemView.setOnClickListener { clickListener.onTrackClick(item) }

        binding.trackName.text = item.trackName
        binding.artistName.text = item.artistName
        binding.trackTime.text = item.trackTimeString

        Glide.with(itemView)
            .load(item.artworkUrl100)
            .transform(CenterCrop(), RoundedCorners(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2F, itemView.resources.displayMetrics).toInt()))
            .placeholder(R.drawable.placeholder)
            .into(binding.trackArtwork)

    }

}