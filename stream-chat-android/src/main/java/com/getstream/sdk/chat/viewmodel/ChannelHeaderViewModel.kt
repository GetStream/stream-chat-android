package com.getstream.sdk.chat.viewmodel

import io.getstream.chat.android.livedata.ChatDomain

/**
 * ViewModel class for [com.getstream.sdk.chat.view.ChannelHeaderView].
 * Responsible for updating channel information.
 * Can be bound to the view using [ChannelHeaderViewModel.bindView] function.
 * @param cid the full channel id, i.e. "messaging:123"
 * @param chatDomain entry point for all livedata & offline operations
 */
public class ChannelHeaderViewModel @JvmOverloads constructor(
    cid: String,
    private val chatDomain: ChatDomain = ChatDomain.instance(),
) : BaseMessageListHeaderViewModel(cid, chatDomain)
