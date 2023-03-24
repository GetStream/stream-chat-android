package io.getstream.chat.android.client.audio

public interface RecordsPlayer {

    public fun onAudioStateChange(hash: String, func: (AudioState) -> Unit)

    public fun onProgressStateChange(hash: String, func: (ProgressData) -> Unit)

    public fun onSpeedChange(hash: String, func: (Float) -> Unit)

    public fun play(sourceUrl: String)

    public fun changeSpeed()

    public fun currentSpeed(): Float

    public fun dispose()
}

public data class ProgressData(public val duration: Int, public val progress: Double)
