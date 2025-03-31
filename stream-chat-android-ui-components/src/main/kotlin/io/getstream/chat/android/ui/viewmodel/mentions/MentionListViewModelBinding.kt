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

@file:JvmName("MentionListViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.mentions

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.feature.mentions.list.MentionListView

/**
 * Binds [MentionListView] with [MentionListViewModel], updating the view's state based on
 * data provided by the ViewModel and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun MentionListViewModel.bindView(view: MentionListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when {
            state.isLoading -> view.showLoading()
            else -> view.showMessages(messages = state.results, isLoadingMore = state.isLoadingMore)
        }
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver { view.showError() },
    )
    view.setLoadMoreListener(::loadMore)
}
