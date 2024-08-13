package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView


class SearchListAdapter(private val showFooter: Boolean) : RecyclerView.Adapter<SearchListViewHolder> () {

    var foundTracks = mutableListOf<Track>()
    var onItemClick: ((Track) -> Unit)? = null
    var onClearHistoryClick: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListViewHolder {

        val view =
            if (viewType == R.layout.activity_search_item)
                LayoutInflater.from(parent.context).inflate(R.layout.activity_search_item, parent, false)
            else
                LayoutInflater.from(parent.context).inflate(R.layout.activity_search_footer, parent, false)
        return SearchListViewHolder(view)

    }

    override fun onBindViewHolder(holder: SearchListViewHolder, position: Int) {

        if (position == foundTracks.size && showFooter) {
            val clearHistoryButton: Button = holder.itemView.findViewById(R.id.clearHistoryButton)
            clearHistoryButton.setOnClickListener {
                onClearHistoryClick?.invoke()
            }
        } else {
            holder.bind(foundTracks[position])
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(foundTracks[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return foundTracks.size + if (showFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == foundTracks.size) R.layout.activity_search_footer else R.layout.activity_search_item
    }

}