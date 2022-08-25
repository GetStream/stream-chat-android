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

package io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input.factory.DateAttachmentFactory
import io.getstream.chat.android.guides.catalog.uicomponents.customattachments.input.factory.QuotedDateAttachmentFactory
import io.getstream.chat.android.guides.cleanup
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.channel.ChannelListActivity
import io.getstream.chat.android.ui.channel.ChannelListFragment
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager

/**
 * An Activity representing a self-contained channel list screen.
 */
class ChannelsActivity : ChannelListActivity(), ChannelListFragment.ChannelListItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChatUI.attachmentFactoryManager = AttachmentFactoryManager(
            attachmentFactories = listOf(
                DateAttachmentFactory()
            )
        )
        ChatUI.quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager(
            quotedAttachmentFactories = listOf(
                QuotedDateAttachmentFactory(),
                DefaultQuotedAttachmentMessageFactory()
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUI.cleanup()
    }

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
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelsActivity::class.java)
        }
    }
}
