package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@InternalStreamChatApi
public class ProgressTracker {

    private val currentProgress: MutableStateFlow<Int> = MutableStateFlow(0)
    private val isComplete: MutableStateFlow<Boolean> = MutableStateFlow(false)

    public var maxValue: Long = 0

    public fun setProgress(progress: Int) {
        currentProgress.value = progress
    }

    public fun currentProgress(): StateFlow<Int> = currentProgress

    public fun setComplete(isComplete: Boolean) {
        this.isComplete.value = isComplete
    }

    public fun isComplete(): StateFlow<Boolean> = isComplete
}
