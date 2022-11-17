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

import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.LinkClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.feature.messages.list.MessageListView.UserClickListener

public sealed interface MessageListListenerContainer {
    public val messageClickListener: MessageClickListener
    public val messageLongClickListener: MessageLongClickListener
    public val messageRetryListener: MessageRetryListener
    public val threadClickListener: MessageListView.ThreadClickListener
    public val attachmentClickListener: AttachmentClickListener
    public val attachmentDownloadClickListener: AttachmentDownloadClickListener
    public val reactionViewClickListener: ReactionViewClickListener
    public val userClickListener: UserClickListener
    public val giphySendListener: GiphySendListener
    public val linkClickListener: LinkClickListener
}
