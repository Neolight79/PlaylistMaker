package com.example.playlistmaker.media.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ListItemBinding
import com.example.playlistmaker.search.domain.models.Track

class FavoriteListAdapter(
    private val clickListener: TrackClickListener
) : RecyclerView.Adapter<FavoriteListViewHolder> () {

    val favoriteTracks = mutableListOf<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteListViewHolder =
        FavoriteListViewHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)

    override fun onBindViewHolder(holder: FavoriteListViewHolder, position: Int) {
        holder.bind(favoriteTracks[position])
    }

    override fun getItemCount(): Int {
        return favoriteTracks.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item
    }

    interface TrackClickListener {
        fun onTrackClick(track: Track)
    }
}