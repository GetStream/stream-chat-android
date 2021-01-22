package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class ProgressTracker {

    private var numberOfLaps = 1
    private var currentProgress: MutableStateFlow<Int> = MutableStateFlow(0)
    private var lapsCompleted: MutableStateFlow<Int> = MutableStateFlow(0)

    public fun initProgress(numberOfLaps: Int = 1) {
        this.numberOfLaps = numberOfLaps
        this.currentProgress.value = 0
        lapsCompleted.value = 0
    }

    public fun getNumberOfLaps(): Int = numberOfLaps

    public fun currentProgress(): StateFlow<Int> = currentProgress

    public fun lapsCompleted(): StateFlow<Int> = lapsCompleted

    public fun setProgress(progress: Int) {
        currentProgress.value = progress
    }

    public fun incrementLap() {
        lapsCompleted.value += 1
    }
}

public fun ProgressTracker.toProgressCallback(): ProgressCallback {
    return object : ProgressCallback {
        override fun onSuccess(file: String) {}

        override fun onError(error: ChatError) {}

        override fun onProgress(progress: Long) {
            setProgress(progress.toInt())
        }
    }
}
