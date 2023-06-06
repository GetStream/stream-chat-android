package io.getstream.chat.android.ui.common.state.messages.composer

public sealed class RecordingState {
    public sealed class Active : RecordingState()
    public object Hold : Active() { override fun toString(): String  = "Recording.Hold" }
    public object Locked : Active() { override fun toString(): String  = "Recording.Locked" }
    public object Idle : RecordingState() { override fun toString(): String  = "Recording.Idle" }
}