package io.getstream.chat.android.ui.message.list.header.viewmodel

import com.getstream.sdk.chat.viewmodel.BaseMessageListHeaderViewModel
import io.getstream.chat.android.livedata.ChatDomain

public class MessageListHeaderViewModel(
    cid: String,
    chatDomain: ChatDomain = ChatDomain.instance(),
) : BaseMessageListHeaderViewModel(cid, chatDomain)
