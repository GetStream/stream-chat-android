package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.ui.gallery.overview.UserMediaAttachment

class ChatInfoSharedMediaViewModel : ViewModel() {
    val userMediaAttachments: MutableLiveData<List<UserMediaAttachment>> = MutableLiveData(emptyList())
}
