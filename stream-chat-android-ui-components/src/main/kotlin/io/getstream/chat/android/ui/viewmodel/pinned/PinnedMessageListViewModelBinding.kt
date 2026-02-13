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

@file:JvmName("PinnedMessageListViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.pinned

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.api.state.EventObserver
import io.getstream.chat.android.ui.feature.pinned.list.PinnedMessageListView

/**
 * Binds [PinnedMessageListView] with [PinnedMessageListViewModel], updating the view's state based on
 * data provided by the ViewModel and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun PinnedMessageListViewModel.bindView(view: PinnedMessageListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        val isLoadingMore = state.results.isNotEmpty() && state.results.last().message.id == ""

        when {
            isLoadingMore -> {
                view.showLoading()
                view.showMessages(state.results)
            }
            state.isLoading -> view.showLoading()
            else -> view.showMessages(state.results)
        }
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError()
        },
    )
    view.setLoadMoreListener {
        this.loadMore()
    }
}
