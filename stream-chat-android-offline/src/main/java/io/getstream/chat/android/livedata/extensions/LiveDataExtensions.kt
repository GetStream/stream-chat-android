package io.getstream.chat.android.livedata.extensions

import androidx.lifecycle.MutableLiveData
import io.getstream.chat.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.withContext

internal suspend fun <T : Any?> MutableLiveData<T>.setOnUi(newValue: T) =
    withContext(DispatcherProvider.Main) {
        value = newValue
    }
