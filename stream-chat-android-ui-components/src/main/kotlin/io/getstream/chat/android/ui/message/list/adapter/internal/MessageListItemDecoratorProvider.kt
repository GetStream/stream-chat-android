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

package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FailedIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FootnoteDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.GapDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MessageContainerMarginDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.PinIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReactionsDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory

/**
 * Provides all decorators that will be used in MessageListView items.
 *
 * @param dateFormatter [DateFormatter]. Formats the dates in the messages.
 * @param isDirectMessage Checks if the message is direct of not. Used in the footnote.
 * @param messageListViewStyle [MessageListViewStyle] The style of the MessageListView and its items.
 * @param showAvatarPredicate [MessageListView.ShowAvatarPredicate] Checks if should show the avatar or not accordingly with the provided logic.
 * @param messageBackgroundFactory [MessageBackgroundFactory] Factory that customizes the background of messages.
 * @param deletedMessageListItemPredicate [MessageListView.MessageListItemPredicate] Predicate to hide or show the the deleted message accordingly to the logic provided.
 * @param isCurrentUserBanned Checks if the current user is banned inside the channel. Used for failed icon indicator.
 */
internal class MessageListItemDecoratorProvider(
    dateFormatter: DateFormatter,
    isDirectMessage: () -> Boolean,
    messageListViewStyle: MessageListViewStyle,
    showAvatarPredicate: MessageListView.ShowAvatarPredicate,
    messageBackgroundFactory: MessageBackgroundFactory,
    deletedMessageListItemPredicate: MessageListView.MessageListItemPredicate,
    isCurrentUserBanned: () -> Boolean
) : DecoratorProvider {

    private val messageListDecorators = listOfNotNull<Decorator>(
        BackgroundDecorator(messageBackgroundFactory),
        TextDecorator(messageListViewStyle.itemStyle),
        GapDecorator(),
        MaxPossibleWidthDecorator(messageListViewStyle.itemStyle),
        MessageContainerMarginDecorator(messageListViewStyle.itemStyle),
        AvatarDecorator(showAvatarPredicate),
        FailedIndicatorDecorator(messageListViewStyle.itemStyle, isCurrentUserBanned),
        ReactionsDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.reactionsEnabled },
        ReplyDecorator(messageListViewStyle.replyMessageStyle),
        FootnoteDecorator(dateFormatter, isDirectMessage, messageListViewStyle, deletedMessageListItemPredicate),
        PinIndicatorDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.pinMessageEnabled },
    )

    override val decorators: List<Decorator> = messageListDecorators
}
