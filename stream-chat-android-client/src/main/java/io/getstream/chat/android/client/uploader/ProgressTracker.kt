package io.getstream.chat.android.client.uploader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.ProgressCallback

public object ProgressTracker {

    private val progressMap: MutableLiveData<MutableMap<String, Int>> = MutableLiveData(mutableMapOf())

    public fun setProgress(id: String, value: Int) {
        progressMap.value = progressMap.value?.apply {
            put(id, value)
        }
    }

    public fun getProgress(id: String): LiveData<Int?> =
        Transformations.map(progressMap) { map ->
            map[id]
        }
}

public fun ProgressTracker.toProgressCallback(id: String): ProgressCallback {
    return object : ProgressCallback {
        override fun onSuccess(file: String) {}

        override fun onError(error: ChatError) {}

        override fun onProgress(progress: Long) {
            setProgress(id, progress.toInt())
        }
    }
}
