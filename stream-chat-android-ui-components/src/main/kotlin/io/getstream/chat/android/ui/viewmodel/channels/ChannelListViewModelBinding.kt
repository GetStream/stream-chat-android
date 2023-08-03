/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.lifecycle.distinctUntilChanged
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.utils.extensions.combineWith

/**
 * Binds [ChannelListView] with [ChannelListViewModel], updating the view's state based on
 * data provided by the ViewModel, and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun ChannelListViewModel.bindView(
    view: ChannelListView,
    lifecycleOwner: LifecycleOwner,
) {
    state.combineWith(paginationState) { state, paginationState -> state to paginationState }
        .combineWith(typingEvents) { states, typingEvents ->
            val state = states?.first
            val paginationState = states?.second

            paginationState?.let {
                view.setPaginationEnabled(!it.endOfChannels && !it.loadingMore)
            }

            var list: List<ChannelListItem> = state?.channels?.map {
                ChannelListItem.ChannelItem(it, typingEvents?.get(it.cid)?.users ?: emptyList())
            } ?: emptyList()
            if (paginationState?.loadingMore == true) {
                list = list + ChannelListItem.LoadingMoreItem
            }

            list to (state?.isLoading == true)
        }.distinctUntilChanged().observe(lifecycleOwner) { (list, isLoading) ->

            when {
                isLoading && list.isEmpty() -> view.showLoadingView()

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
