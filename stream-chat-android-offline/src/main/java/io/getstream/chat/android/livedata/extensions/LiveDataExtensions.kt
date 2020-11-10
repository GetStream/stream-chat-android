package io.getstream.chat.android.livedata.extensions

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T : Any?> MutableLiveData<T>.setOnUi(newValue: T) =
    withContext(Dispatchers.Main) {
        value = newValue
    }
