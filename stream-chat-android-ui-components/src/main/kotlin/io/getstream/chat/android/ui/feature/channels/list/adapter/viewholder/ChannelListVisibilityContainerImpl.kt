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

package io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView.ChannelOptionVisibilityPredicate
import io.getstream.chat.android.ui.utils.ListenerDelegate

internal class ChannelListVisibilityContainerImpl(
    isMoreOptionsVisible: ChannelOptionVisibilityPredicate = moreOptionsDefault,
    isDeleteOptionVisible: ChannelOptionVisibilityPredicate = deleteOptionDefault,
) : ChannelListVisibilityContainer {

    override var isMoreOptionsVisible: ChannelOptionVisibilityPredicate by ListenerDelegate(isMoreOptionsVisible) { realPredicate ->
        ChannelOptionVisibilityPredicate { channel ->
            realPredicate().invoke(channel)
        }
    }

    override var isDeleteOptionVisible: ChannelOptionVisibilityPredicate by ListenerDelegate(isDeleteOptionVisible) { realPredicate ->
        ChannelOptionVisibilityPredicate { channel ->
            realPredicate().invoke(channel)
        }
    }

    private companion object {
        val moreOptionsDefault: ChannelOptionVisibilityPredicate = ChannelOptionVisibilityPredicate {
            // "more options" is visible by default
            true
        }

        val deleteOptionDefault: ChannelOptionVisibilityPredicate = ChannelOptionVisibilityPredicate {
            // "delete option" is visible if the channel's ownCapabilities contains the delete capability
            it.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)
        }
    }
}
