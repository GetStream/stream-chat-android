package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.AttachmentWithDate

class ChatInfoSharedMediaViewModel : ViewModel() {

    val attachmentWithDate: MutableLiveData<List<AttachmentWithDate>> = MutableLiveData(emptyList())
}
