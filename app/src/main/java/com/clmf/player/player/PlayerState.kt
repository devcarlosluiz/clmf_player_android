package com.clmf.player.player

sealed class PlayerState {
    data object Idle : PlayerState()
    data object Loading : PlayerState()
    data object Buffering : PlayerState()
    data object Playing : PlayerState()
    data object Paused : PlayerState()
    data class Retrying(val attempt: Int, val maxAttempts: Int) : PlayerState()
    data class Error(val message: String) : PlayerState()
    data object Ended : PlayerState()
}

data class PlaybackProgress(
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val bufferedPercentage: Int = 0
)
