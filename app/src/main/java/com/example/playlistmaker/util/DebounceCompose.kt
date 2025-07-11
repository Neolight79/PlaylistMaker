package com.example.playlistmaker.util

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun debounceCompose(debounceTime: Long = 1000L, action: () -> Unit): () -> Unit {
    val debouncedAction = remember(action) {
        var lastClickTime: Long = 0
        {
            if (SystemClock.elapsedRealtime() - lastClickTime >= debounceTime) {
                lastClickTime = SystemClock.elapsedRealtime()
                action()
            }
        }
    }
    return debouncedAction
}
