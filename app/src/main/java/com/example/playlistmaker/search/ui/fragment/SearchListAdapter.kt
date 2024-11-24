package com.example.playlistmaker.search.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ListSearchFooterBinding
import com.example.playlistmaker.databinding.ListSearchItemBinding
import com.example.playlistmaker.search.domain.models.Track

class SearchListAdapter(private val showFooter: Boolean, private val clickListener: TrackClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {

    var foundTracks = mutableListOf<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.list_search_footer -> SearchFooterViewHolder(ListSearchFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
            else -> SearchListViewHolder(ListSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchListViewHolder) {
            holder.bind(foundTracks[position])
        }
        if (holder is SearchFooterViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return foundTracks.size + if (showFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == foundTracks.size) R.layout.list_search_footer else R.layout.list_search_item
    }

    interface TrackClickListener {
        fun onTrackClick(track: Track)
        fun onClearHistoryClick()
    }
}