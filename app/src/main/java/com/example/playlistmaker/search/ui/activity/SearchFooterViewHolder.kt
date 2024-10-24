package com.example.playlistmaker.search.ui.activity

import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.ActivitySearchFooterBinding

class SearchFooterViewHolder(private val binding: ActivitySearchFooterBinding,
                             private val clickListener: SearchListAdapter.TrackClickListener
): RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        binding.clearHistoryButton.setOnClickListener {
            clickListener.onClearHistoryClick()
        }
    }

}