package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Attachment

class ChatInfoSharedMediaViewModel : ViewModel() {

    val attachments: MutableLiveData<List<Attachment>> = MutableLiveData(emptyList())
}
