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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.feature.messages.list.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger

internal class MessagePlainTextViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val messageTextTransformer: ChatMessageTextTransformer,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.streamThemeInflater,
            parent,
            false,
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    private val logger by taggedLogger("Chat:MessagePlainTextVH")

    init {
        binding.run {
            listeners?.let { container ->
                messageContainer.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                messageContainer.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                userAvatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
                messageText.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                LongClickFriendlyLinkMovementMethod.set(
                    textView = messageText,
                    longClickTarget = messageContainer,
                    onLinkClicked = container.linkClickListener::onLinkClick,
                )
            }
        }
    }

    override fun messageContainerView(): View = binding.messageContainer

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)
        val textUnchanged = diff?.text == false
        val mentionsUnchanged = diff?.mentions == false
        if (textUnchanged && mentionsUnchanged) return

        with(binding) {
            logger.d { "[bindData] isVisible: ${messageItemView.isVisible}, w: ${messageItemView.width}, " +
                "h: ${messageItemView.height}" }
            messageTextTransformer.transformAndApply(messageText, data)
            messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                horizontalBias = if (data.isTheirs) 0f else 1f
            }
            messageItemView.doOnPreDraw {
                logger.v { "[bindData] isVisible: ${messageItemView.isVisible}, w: ${messageItemView.width}, " +
                    "h: ${messageItemView.height}" }
            }
            messageItemView
        }
    }
}
