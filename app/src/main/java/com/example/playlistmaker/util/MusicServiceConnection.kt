package com.example.playlistmaker.util

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.example.playlistmaker.player.service.MusicService
import com.example.playlistmaker.player.ui.view_model.PlayerViewModel

class MusicServiceConnection(private val viewModel: PlayerViewModel) : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MusicServiceBinder
        viewModel.setAudioPlayerControl(binder.getService())
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        viewModel.removeAudioPlayerControl()
    }
}
