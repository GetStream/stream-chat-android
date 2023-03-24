package io.getstream.chat.android.client.audio

public interface RecordsPlayer {

    public fun onAudioStateChange(hash: Int, func: (AudioState) -> Unit)

    public fun onProgressStateChange(hash: Int, func: (ProgressData) -> Unit)

    public fun onSpeedChange(hash: Int, func: (Float) -> Unit)

    public fun play(sourceUrl: String, audioHash: Int)

    public fun changeSpeed()

    public fun currentSpeed(): Float

    public fun dispose()
}

public data class ProgressData(public val duration: Int, public val progress: Double)
