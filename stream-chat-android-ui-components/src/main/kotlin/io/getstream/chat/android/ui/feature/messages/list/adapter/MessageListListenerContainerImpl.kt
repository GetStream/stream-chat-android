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
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.UserClickListener
import io.getstream.chat.android.ui.utils.ListenerDelegate

internal class MessageListListenerContainerImpl(
    messageClickListener: MessageClickListener = MessageClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListener: MessageLongClickListener = MessageLongClickListener(EmptyFunctions.ONE_PARAM),
    messageRetryListener: MessageRetryListener = MessageRetryListener(EmptyFunctions.ONE_PARAM),
    threadClickListener: ThreadClickListener = ThreadClickListener(EmptyFunctions.ONE_PARAM),
    attachmentClickListener: AttachmentClickListener = AttachmentClickListener(EmptyFunctions.TWO_PARAM),
    attachmentDownloadClickListener: AttachmentDownloadClickListener = AttachmentDownloadClickListener(EmptyFunctions.ONE_PARAM),
    reactionViewClickListener: ReactionViewClickListener = ReactionViewClickListener(EmptyFunctions.ONE_PARAM),
    userClickListener: UserClickListener = UserClickListener(EmptyFunctions.ONE_PARAM),
    giphySendListener: GiphySendListener = GiphySendListener(EmptyFunctions.ONE_PARAM),
    linkClickListener: LinkClickListener = LinkClickListener(EmptyFunctions.ONE_PARAM),
) : MessageListListenerContainer {
    private object EmptyFunctions {
        val ONE_PARAM: (Any) -> Unit = { _ -> }
        val TWO_PARAM: (Any, Any) -> Unit = { _, _ -> }
    }

    override var messageClickListener: MessageClickListener by ListenerDelegate(
        messageClickListener,
    ) { realListener ->
        MessageClickListener { message ->
            realListener().onMessageClick(message)
        }
    }

    override var messageLongClickListener: MessageLongClickListener by ListenerDelegate(
        messageLongClickListener,
    ) { realListener ->
        MessageLongClickListener { message ->
            realListener().onMessageLongClick(message)
        }
    }

    override var messageRetryListener: MessageRetryListener by ListenerDelegate(
        messageRetryListener,
    ) { realListener ->
        MessageRetryListener { message ->
            realListener().onRetryMessage(message)
        }
    }

    override var threadClickListener: ThreadClickListener by ListenerDelegate(
        threadClickListener,
    ) { realListener ->
        ThreadClickListener { message ->
            realListener().onThreadClick(message)
        }
    }

    override var attachmentClickListener: AttachmentClickListener by ListenerDelegate(
        attachmentClickListener,
    ) { realListener ->
        AttachmentClickListener { message, attachment ->
            realListener().onAttachmentClick(message, attachment)
        }
    }

    override var attachmentDownloadClickListener: AttachmentDownloadClickListener by ListenerDelegate(
        attachmentDownloadClickListener,
    ) { realListener ->
        AttachmentDownloadClickListener { attachment ->
            realListener().onAttachmentDownloadClick(attachment)
        }
    }

    override var reactionViewClickListener: ReactionViewClickListener by ListenerDelegate(
        reactionViewClickListener,
    ) { realListener ->
        ReactionViewClickListener { message ->
            realListener().onReactionViewClick(message)
        }
    }

    override var userClickListener: UserClickListener by ListenerDelegate(
        userClickListener,
    ) { realListener ->
        UserClickListener { user ->
            realListener().onUserClick(user)
        }
    }

    override var giphySendListener: GiphySendListener by ListenerDelegate(
        giphySendListener,
    ) { realListener ->
        GiphySendListener { action ->
            realListener().onGiphySend(action)
        }
    }

    override var linkClickListener: LinkClickListener by ListenerDelegate(
        linkClickListener,
    ) { realListener ->
        LinkClickListener { url ->
            realListener().onLinkClick(url)
        }
    }
}
