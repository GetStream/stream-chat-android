/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("ChannelListViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.channels

import android.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.distinctUntilChanged
import io.getstream.chat.android.client.api.state.EventObserver
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.viewmodel.channels.internal.ChannelListBindingData

/**
 * Binds [ChannelListView] with [ChannelListViewModel], updating the view's state based on
 * data provided by the ViewModel, and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
@Suppress("LongMethod")
public fun ChannelListViewModel.bindView(
    view: ChannelListView,
    lifecycleOwner: LifecycleOwner,
) {
    val mediatorLiveData = MediatorLiveData(ChannelListBindingData())
    mediatorLiveData.addSource(state) {
        mediatorLiveData.value = mediatorLiveData.value?.copy(state = it)
    }
    mediatorLiveData.addSource(paginationState) {
        mediatorLiveData.value = mediatorLiveData.value?.copy(paginationState = it)
    }
    mediatorLiveData.addSource(typingEvents) {
        mediatorLiveData.value = mediatorLiveData.value?.copy(typingEvents = it)
    }
    mediatorLiveData.addSource(draftMessages) {
        mediatorLiveData.value = mediatorLiveData.value?.copy(draftMessages = it)
    }
    mediatorLiveData
        .distinctUntilChanged()
        .observe(lifecycleOwner) {
            with(it) {
                view.setPaginationEnabled(!paginationState.endOfChannels && !paginationState.loadingMore)

                val list: List<ChannelListItem> = state.channels.map { channel ->
                    ChannelListItem.ChannelItem(
                        channel = channel,
                        typingUsers = typingEvents[channel.cid]?.users ?: emptyList(),
                        draftMessage = draftMessages[channel.cid],
                    )
                } + listOfNotNull(ChannelListItem.LoadingMoreItem.takeIf { paginationState.loadingMore })

                when {
                    state.isLoading && list.isEmpty() -> view.showLoadingView()

                    list.isNotEmpty() -> {
                        view.hideLoadingView()
                        view.setChannels(list)
                    }

                    else -> {
                        view.hideLoadingView()
                        view.setChannels(emptyList())
                    }
                }
            }
        }

    view.setOnEndReachedListener {
        onAction(ChannelListViewModel.Action.ReachedEndOfList)
    }

    view.setChannelDeleteClickListener {
        AlertDialog.Builder(view.context)
            .setTitle(R.string.stream_ui_channel_list_delete_confirmation_title)
            .setMessage(R.string.stream_ui_channel_list_delete_confirmation_message)
            .setPositiveButton(R.string.stream_ui_channel_list_delete_confirmation_positive_button) { dialog, _ ->
                dialog.dismiss()
                deleteChannel(it)
            }
            .setNegativeButton(R.string.stream_ui_channel_list_delete_confirmation_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    view.setChannelLeaveClickListener { channel ->
        leaveChannel(channel)
    }

    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError(it)
        },
    )
}
