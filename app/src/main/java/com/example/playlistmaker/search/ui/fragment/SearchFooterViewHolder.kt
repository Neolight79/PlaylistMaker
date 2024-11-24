package com.example.playlistmaker.search.ui.fragment

import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.ListSearchFooterBinding

class SearchFooterViewHolder(private val binding: ListSearchFooterBinding,
                             private val clickListener: SearchListAdapter.TrackClickListener
): RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        binding.clearHistoryButton.setOnClickListener {
            clickListener.onClearHistoryClick()
        }
    }

}