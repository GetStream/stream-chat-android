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

@file:JvmName("ChannelListHeaderViewModelBinding")

package io.getstream.chat.android.ui.viewmodel.channels

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView

/**
 * Binds [ChannelListHeaderView] with [ChannelListHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel, and propagating view events to the ViewModel as needed.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun ChannelListHeaderViewModel.bindView(view: ChannelListHeaderView, lifecycleOwner: LifecycleOwner) {
    with(view) {
        currentUser.observe(lifecycleOwner) { user ->
            user?.let(::setUser)
        }
        connectionState.observe(lifecycleOwner) { connectionState ->
            when (connectionState) {
                is ConnectionState.Connected -> showOnlineTitle()
                is ConnectionState.Connecting -> showConnectingTitle()
                is ConnectionState.Offline -> showOfflineTitle()
            }
        }
    }
}
