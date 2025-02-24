package com.example.playlistmaker.media.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ListItemBinding
import com.example.playlistmaker.search.domain.models.Track

class TracksListAdapter(
    private val clickListener: TrackClickListener
) : RecyclerView.Adapter<TracksListViewHolder> () {

    val tracksList = mutableListOf<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksListViewHolder =
        TracksListViewHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)

    override fun onBindViewHolder(holder: TracksListViewHolder, position: Int) {
        holder.bind(tracksList[position])
    }

    override fun getItemCount(): Int {
        return tracksList.size
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item
    }

    interface TrackClickListener {
        fun onTrackClick(track: Track)
        fun onTrackLongClick(track: Track)
    }
}