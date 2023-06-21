package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.Attachment

public sealed class RecordingState {

    public object Idle : RecordingState() {
        override fun toString(): String = "Recording.Idle"
    }

    public sealed class Recording : RecordingState() {
        public abstract val durationInMs: Int
        public abstract val waveform: List<Float>
    }

    public data class Hold(
        override val durationInMs: Int = 0,
        override val waveform: List<Float> = emptyList(),
    ) : Recording() {
        override fun toString(): String = "Recording.Hold(waveform=${waveform.size}, duration=${durationInMs}ms)"
    }

    public data class Locked(
        override val durationInMs: Int = 0,
        override val waveform: List<Float> = emptyList(),
    ) : Recording() {
        override fun toString(): String = "Recording.Locked(waveform=${waveform.size}, duration=${durationInMs}ms)"
    }

    public data class Overview(
        val durationInMs: Int = 0,
        val waveform: List<Float> = emptyList(),
        val attachment: Attachment,
        val isPlaying: Boolean = false,
        val playingProgress: Float = 0f,
        val playingId: Int = -1,
    ) : RecordingState() {
        override fun toString(): String = "Recording.Overview(" +
            "waveform=${waveform.size}, " +
            "duration=${durationInMs}ms, " +
            "isPlaying=${isPlaying}, " +
            "playingProgress=${playingProgress}, " +
            "attachment=${attachment.upload}" +
            ")"
    }

    public data class Complete(
        val attachment: Attachment,
    ) : RecordingState() {
        override fun toString(): String = "Recording.Complete(" +
            "attachment=${attachment.upload}" +
            ")"
    }
}

public fun RecordingState.Recording.copy(
    duration: Int = this.durationInMs,
    waveform: List<Float> = this.waveform,
): RecordingState.Recording = when (this) {
    is RecordingState.Hold -> RecordingState.Hold(duration, waveform)
    is RecordingState.Locked -> RecordingState.Locked(duration, waveform)
}