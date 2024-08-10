package com.example.playlistmaker

import com.google.gson.annotations.SerializedName

class ItunesResponse(
    val resultCount: Int,
    val results: MutableList<Track>) {
}