package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.livedata.ChatDomain

class ChatViewModel : ViewModel() {
    private val chatDomain: ChatDomain = ChatDomain.instance()

    val online: LiveData<Boolean> = chatDomain.online
}