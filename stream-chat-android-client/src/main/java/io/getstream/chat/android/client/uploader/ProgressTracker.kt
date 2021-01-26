package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

public class ProgressTracker {

    private var numberOfItems = 1
    private val currentItemProgress: MutableStateFlow<Int> = MutableStateFlow(0)
    private val lapsCompleted: MutableStateFlow<Int> = MutableStateFlow(0)

    public fun initProgress(numberOfItems: Int = 1) {
        this.numberOfItems = numberOfItems
        this.currentItemProgress.value = 0
        lapsCompleted.value = 0
    }

    public fun getNumberOfItems(): Int = numberOfItems

    public fun currentItemProgress(): StateFlow<Int> = currentItemProgress

    public fun lapsCompleted(): StateFlow<Int> = lapsCompleted

    public fun setProgress(progress: Int) {
        currentItemProgress.value = progress
    }

    public fun incrementCompletedItems() {
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
