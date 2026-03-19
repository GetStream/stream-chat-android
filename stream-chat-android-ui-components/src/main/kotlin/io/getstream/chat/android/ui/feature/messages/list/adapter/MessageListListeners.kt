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

public sealed interface MessageListListeners {
    public val messageClickListener: OnMessageClickListener
    public val messageLongClickListener: OnMessageLongClickListener
    public val messageRetryListener: OnMessageRetryListener
    public val threadClickListener: OnThreadClickListener
    public val translatedLabelClickListener: OnTranslatedLabelClickListener
    public val attachmentClickListener: OnAttachmentClickListener
    public val attachmentDownloadClickListener: OnAttachmentDownloadClickListener
    public val reactionViewClickListener: OnReactionViewClickListener
    public val userClickListener: OnUserClickListener
    public val mentionClickListener: OnMentionClickListener
    public val giphySendListener: OnGiphySendListener
    public val linkClickListener: OnLinkClickListener
    public val unreadLabelReachedListener: OnUnreadLabelReachedListener
    public val onPollOptionClickListener: OnPollOptionClickListener
    public val onShowAllPollOptionClickListener: OnShowAllPollOptionClickListener
    public val onPollCloseClickListener: OnPollCloseClickListener
    public val onViewPollResultClickListener: OnViewPollResultClickListener
}
