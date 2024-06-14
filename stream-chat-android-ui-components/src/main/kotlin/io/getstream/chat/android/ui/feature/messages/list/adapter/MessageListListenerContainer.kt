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

package io.getstream.chat.android.ui.feature.messages.list.adapter

import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.LinkClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnAttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnAttachmentDownloadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnGiphySendListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnLinkClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageRetryListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnPollCloseClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnPollOptionClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnThreadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnUnreadLabelReachedListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnUserClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnViewPollResultClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.UserClickListener

@Deprecated(
    message = "Use MessageListListeners instead",
    replaceWith = ReplaceWith("MessageListListeners"),
    level = DeprecationLevel.WARNING,
)
public sealed interface MessageListListenerContainer {
    public val messageClickListener: MessageClickListener
    public val messageLongClickListener: MessageLongClickListener
    public val messageRetryListener: MessageRetryListener
    public val threadClickListener: ThreadClickListener
    public val attachmentClickListener: AttachmentClickListener
    public val attachmentDownloadClickListener: AttachmentDownloadClickListener
    public val reactionViewClickListener: ReactionViewClickListener
    public val userClickListener: UserClickListener
    public val giphySendListener: GiphySendListener
    public val linkClickListener: LinkClickListener
}

public sealed interface MessageListListeners {
    public val messageClickListener: OnMessageClickListener
    public val messageLongClickListener: OnMessageLongClickListener
    public val messageRetryListener: OnMessageRetryListener
    public val threadClickListener: OnThreadClickListener
    public val attachmentClickListener: OnAttachmentClickListener
    public val attachmentDownloadClickListener: OnAttachmentDownloadClickListener
    public val reactionViewClickListener: OnReactionViewClickListener
    public val userClickListener: OnUserClickListener
    public val giphySendListener: OnGiphySendListener
    public val linkClickListener: OnLinkClickListener
    public val unreadLabelReachedListener: OnUnreadLabelReachedListener
    public val onPollOptionClickListener: OnPollOptionClickListener
    public val onPollCloseClickListener: OnPollCloseClickListener
    public val onViewPollResultClickListener: OnViewPollResultClickListener
}

@Deprecated(
    message = "Remove once MessageListListenerContainer is deleted",
    level = DeprecationLevel.WARNING,
)
internal class MessageListListenersAdapter(
    private val adaptee: MessageListListeners,
) : MessageListListenerContainer {
    override val messageClickListener: MessageClickListener = MessageClickListener {
        adaptee.messageClickListener.onMessageClick(it)
    }
    override val messageLongClickListener: MessageLongClickListener = MessageLongClickListener {
        adaptee.messageLongClickListener.onMessageLongClick(it)
    }
    override val messageRetryListener: MessageRetryListener = MessageRetryListener {
        adaptee.messageRetryListener.onRetryMessage(it)
    }
    override val threadClickListener: ThreadClickListener = ThreadClickListener {
        adaptee.threadClickListener.onThreadClick(it)
    }
    override val attachmentClickListener: AttachmentClickListener = AttachmentClickListener { message, attachment ->
        adaptee.attachmentClickListener.onAttachmentClick(message, attachment)
    }
    override val attachmentDownloadClickListener: AttachmentDownloadClickListener = AttachmentDownloadClickListener {
        adaptee.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
    }
    override val reactionViewClickListener: ReactionViewClickListener = ReactionViewClickListener {
        adaptee.reactionViewClickListener.onReactionViewClick(it)
    }
    override val userClickListener: UserClickListener = UserClickListener {
        adaptee.userClickListener.onUserClick(it)
    }
    override val giphySendListener: GiphySendListener = GiphySendListener {
        adaptee.giphySendListener.onGiphySend(it)
    }
    override val linkClickListener: LinkClickListener = LinkClickListener {
        adaptee.linkClickListener.onLinkClick(it)
    }
}
