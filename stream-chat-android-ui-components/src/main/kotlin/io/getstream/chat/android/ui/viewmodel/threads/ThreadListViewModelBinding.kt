/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

@file:JvmName("ThreadListViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.threads

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.feature.threads.list.ThreadListView

/**
 * Binds [ThreadListView] to a [ThreadListViewModel], updating the view's state based on
 * data provided by the ViewModel and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun ThreadListViewModel.bindView(view: ThreadListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when {
            state.threads.isEmpty() && state.isLoading -> view.showLoading()
            else -> view.showThreads(state.threads, state.isLoadingMore)
        }
        view.showUnreadThreadsBanner(state.unseenThreadsCount)
    }
    view.setUnreadThreadsBannerClickListener(::load)
    view.setLoadMoreListener(::loadNextPage)
}
