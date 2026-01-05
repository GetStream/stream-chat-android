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

package io.getstream.chat.android.ui.feature.messages.list.adapter

import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnAttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnAttachmentDownloadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnGiphySendListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnLinkClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMentionClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnMessageRetryListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnPollCloseClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnPollOptionClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnShowAllPollOptionClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnThreadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnTranslatedLabelClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnUnreadLabelReachedListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnUserClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.OnViewPollResultClickListener
import io.getstream.chat.android.ui.utils.ListenerDelegate

internal class MessageListListenerContainerImpl(
    messageClickListener: OnMessageClickListener = OnMessageClickListener(EmptyFunctions.ONE_PARAM),
    messageLongClickListener: OnMessageLongClickListener = OnMessageLongClickListener(EmptyFunctions.ONE_PARAM),
    messageRetryListener: OnMessageRetryListener = OnMessageRetryListener(EmptyFunctions.ONE_PARAM),
    threadClickListener: OnThreadClickListener = OnThreadClickListener(EmptyFunctions.ONE_PARAM),
    translatedLabelClickListener: OnTranslatedLabelClickListener = OnTranslatedLabelClickListener(
        EmptyFunctions.ONE_PARAM,
    ),
    attachmentClickListener: OnAttachmentClickListener = OnAttachmentClickListener(EmptyFunctions.TWO_PARAM),
    attachmentDownloadClickListener: OnAttachmentDownloadClickListener = OnAttachmentDownloadClickListener(
        EmptyFunctions.ONE_PARAM,
    ),
    reactionViewClickListener: OnReactionViewClickListener = OnReactionViewClickListener(EmptyFunctions.ONE_PARAM),
    userClickListener: OnUserClickListener = OnUserClickListener(EmptyFunctions.ONE_PARAM),
    mentionClickListener: OnMentionClickListener = OnMentionClickListener(EmptyFunctions.ONE_PARAM),
    giphySendListener: OnGiphySendListener = OnGiphySendListener(EmptyFunctions.ONE_PARAM),
    linkClickListener: OnLinkClickListener = OnLinkClickListener(EmptyFunctions.ONE_PARAM),
    onUnreadLabelReachedListener: OnUnreadLabelReachedListener = OnUnreadLabelReachedListener { },
    onPollOptionClickListener: OnPollOptionClickListener,
    onShowAllPollOptionClickListener: OnShowAllPollOptionClickListener,
    onPollCloseClickListener: OnPollCloseClickListener,
    onViewPollResultClickListener: OnViewPollResultClickListener,
) : MessageListListeners {
    private object EmptyFunctions {
        val ONE_PARAM: (Any) -> Boolean = { _ -> false }
        val TWO_PARAM: (Any, Any) -> Boolean = { _, _ -> false }
    }

    override var messageClickListener: OnMessageClickListener by ListenerDelegate(
        messageClickListener,
    ) { realListener ->
        OnMessageClickListener { message ->
            realListener().onMessageClick(message)
        }
    }

    override var messageLongClickListener: OnMessageLongClickListener by ListenerDelegate(
        messageLongClickListener,
    ) { realListener ->
        OnMessageLongClickListener { message ->
            realListener().onMessageLongClick(message)
        }
    }

    override var messageRetryListener: OnMessageRetryListener by ListenerDelegate(
        messageRetryListener,
    ) { realListener ->
        OnMessageRetryListener { message ->
            realListener().onRetryMessage(message)
        }
    }

    override var threadClickListener: OnThreadClickListener by ListenerDelegate(
        threadClickListener,
    ) { realListener ->
        OnThreadClickListener { message ->
            realListener().onThreadClick(message)
        }
    }

    override val translatedLabelClickListener: OnTranslatedLabelClickListener by ListenerDelegate(
        translatedLabelClickListener,
    ) { realListener ->
        OnTranslatedLabelClickListener { message ->
            realListener().onTranslatedLabelClick(message)
        }
    }

    override var attachmentClickListener: OnAttachmentClickListener by ListenerDelegate(
        attachmentClickListener,
    ) { realListener ->
        OnAttachmentClickListener { message, attachment ->
            realListener().onAttachmentClick(message, attachment)
        }
    }

    override var attachmentDownloadClickListener: OnAttachmentDownloadClickListener by ListenerDelegate(
        attachmentDownloadClickListener,
    ) { realListener ->
        OnAttachmentDownloadClickListener { attachment ->
            realListener().onAttachmentDownloadClick(attachment)
        }
    }

    override var reactionViewClickListener: OnReactionViewClickListener by ListenerDelegate(
        reactionViewClickListener,
    ) { realListener ->
        OnReactionViewClickListener { message ->
            realListener().onReactionViewClick(message)
        }
    }

    override var userClickListener: OnUserClickListener by ListenerDelegate(
        userClickListener,
    ) { realListener ->
        OnUserClickListener { user ->
            realListener().onUserClick(user)
        }
    }

    override var mentionClickListener: OnMentionClickListener by ListenerDelegate(
        mentionClickListener,
    ) { realListener ->
        OnMentionClickListener { user ->
            realListener().onMentionClick(user)
        }
    }

    override var giphySendListener: OnGiphySendListener by ListenerDelegate(
        giphySendListener,
    ) { realListener ->
        OnGiphySendListener { action ->
            realListener().onGiphySend(action)
        }
    }

    override var linkClickListener: OnLinkClickListener by ListenerDelegate(
        linkClickListener,
    ) { realListener ->
        OnLinkClickListener { url ->
            realListener().onLinkClick(url)
        }
    }

    override var unreadLabelReachedListener: OnUnreadLabelReachedListener by ListenerDelegate(
        onUnreadLabelReachedListener,
    ) { realListener ->
        OnUnreadLabelReachedListener {
            realListener().onUnreadLabelReached()
        }
    }

    override var onPollOptionClickListener: OnPollOptionClickListener by ListenerDelegate(
        onPollOptionClickListener,
    ) { realListener ->
        OnPollOptionClickListener { message, poll, option ->
            realListener().onPollOptionClick(message, poll, option)
        }
    }

    override var onShowAllPollOptionClickListener: OnShowAllPollOptionClickListener by ListenerDelegate(
        onShowAllPollOptionClickListener,
    ) { realListener ->
        OnShowAllPollOptionClickListener { message, poll ->
            realListener().onShowAllPollOptionClick(message, poll)
        }
    }

    override var onPollCloseClickListener: OnPollCloseClickListener by ListenerDelegate(
        onPollCloseClickListener,
    ) { realListener ->
        OnPollCloseClickListener { poll ->
            realListener().onPollCloseClick(poll)
        }
    }

    override var onViewPollResultClickListener: OnViewPollResultClickListener by ListenerDelegate(
        onViewPollResultClickListener,
    ) { realListener ->
        OnViewPollResultClickListener { poll ->
            realListener().onViewPollResultClick(poll)
        }
    }
}
