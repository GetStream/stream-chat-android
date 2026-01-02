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

package io.getstream.chat.android.ui.feature.messages.list.adapter.internal

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.FailedIndicatorDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.FootnoteDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.GapDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.MessageContainerMarginDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.PinIndicatorDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.ReactionsDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory
import io.getstream.chat.android.ui.utils.extensions.isCurrentUserBanned

/**
 * Provides all decorators that will be used in MessageListView items.
 *
 * @param channel [Channel].
 * @param dateFormatter [DateFormatter]. Formats the dates in the messages.
 * @param messageListViewStyle [MessageListViewStyle] The style of the MessageListView and its items.
 * @param showAvatarPredicate [MessageListView.ShowAvatarPredicate] Checks if should show the avatar or not accordingly with the provided logic.
 * @param messageBackgroundFactory [MessageBackgroundFactory] Factory that customizes the background of messages.
 * @param deletedMessageVisibility [DeletedMessageVisibility] Used to hide or show the the deleted message accordingly to the logic provided.
 */
@Suppress("LongParameterList")
internal class MessageListItemDecoratorProvider(
    channel: Channel,
    dateFormatter: DateFormatter,
    messageListViewStyle: MessageListViewStyle,
    showAvatarPredicate: MessageListView.ShowAvatarPredicate,
    messageBackgroundFactory: MessageBackgroundFactory,
    deletedMessageVisibility: () -> DeletedMessageVisibility,
    getLanguageDisplayName: (code: String) -> String,
    decoratorPredicate: (Decorator) -> Boolean,
) : DecoratorProvider {

    override val decorators: List<Decorator> by lazy {
        listOfNotNull<Decorator>(
            BackgroundDecorator(messageBackgroundFactory),
            TextDecorator(messageListViewStyle.itemStyle),
            GapDecorator(),
            MaxPossibleWidthDecorator(messageListViewStyle.itemStyle),
            MessageContainerMarginDecorator(messageListViewStyle.itemStyle),
            AvatarDecorator(showAvatarPredicate),
            FailedIndicatorDecorator(messageListViewStyle.itemStyle) { channel.isCurrentUserBanned() },
            ReactionsDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.reactionsEnabled },
            ReplyDecorator(messageListViewStyle.replyMessageStyle),
            FootnoteDecorator(
                dateFormatter,
                { channel.isDirectMessaging() },
                { channel.config.isThreadEnabled },
                messageListViewStyle,
                deletedMessageVisibility,
                getLanguageDisplayName,
            ),
            PinIndicatorDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.pinMessageEnabled },
        ).filter(decoratorPredicate)
    }
}
