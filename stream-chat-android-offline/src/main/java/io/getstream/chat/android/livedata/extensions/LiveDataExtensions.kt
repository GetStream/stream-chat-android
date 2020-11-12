package io.getstream.chat.android.livedata.extensions

import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.client.internal.DispatcherProvider
import kotlinx.coroutines.withContext

internal suspend fun <T : Any?> MutableLiveData<T>.setOnUi(newValue: T) =
    withContext(DispatcherProvider.Main) {
        value = newValue
    }
