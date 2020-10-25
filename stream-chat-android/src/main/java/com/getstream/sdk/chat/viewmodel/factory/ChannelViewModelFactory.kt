package com.getstream.sdk.chat.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel

@Suppress("UNCHECKED_CAST")
/**
 * A ViewModel factory for MessageListViewModel, ChannelHeaderViewModel and MessageInputViewModel
 *
 * @param cid: the channel id in the format messaging:123
 *
 * @see MessageListViewModel
 * @see ChannelHeaderViewModel
 * @see MessageInputViewModel
 */
public class ChannelViewModelFactory(private val cid: String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when (modelClass) {
        MessageListViewModel::class.java -> MessageListViewModel(cid) as T
        ChannelHeaderViewModel::class.java -> ChannelHeaderViewModel(cid) as T
        MessageInputViewModel::class.java -> MessageInputViewModel(cid) as T
        else -> super.create(modelClass)
    }
}
