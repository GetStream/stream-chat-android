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

package io.getstream.chat.android.guides.catalog.uicomponents.messagesscreen

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity
import io.getstream.chat.android.ui.feature.channels.ChannelListFragment

/**
 * An Activity representing a self-contained channel list screen.
 */
class ChannelsActivity :
    ChannelListActivity(),
    ChannelListFragment.ChannelListItemClickListener {

    /**
     * A callback that handles channel item clicks.
     *
     * @param channel The selected channel.
     */
    override fun onChannelClick(channel: Channel) {
        startActivity(MessagesActivity.createIntent(this, channel.cid))
    }

    companion object {
        /**
         * Creates an [Intent] to start [ChannelsActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [ChannelsActivity].
         */
        fun createIntent(context: Context): Intent = Intent(context, ChannelsActivity::class.java)
    }
}
