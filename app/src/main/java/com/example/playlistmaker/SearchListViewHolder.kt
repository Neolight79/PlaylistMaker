package com.example.playlistmaker

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SearchListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTime: TextView
    private lateinit var trackArtwork: ImageView

    fun bind(item: Track) {

        trackName = itemView.findViewById(R.id.trackName)
        artistName = itemView.findViewById(R.id.artistName)
        trackTime = itemView.findViewById(R.id.trackTime)
        trackArtwork = itemView.findViewById(R.id.trackArtwork)

        trackName.text = item.trackName
        artistName.text = item.artistName
        trackTime.text = item.trackTimeString
        Glide.with(itemView)
            .load(item.artworkUrl100)
            .transform(CenterCrop(), RoundedCorners(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                2F, itemView.resources.displayMetrics).toInt()))
            .placeholder(R.drawable.placeholder)
            .into(trackArtwork)
    }

}