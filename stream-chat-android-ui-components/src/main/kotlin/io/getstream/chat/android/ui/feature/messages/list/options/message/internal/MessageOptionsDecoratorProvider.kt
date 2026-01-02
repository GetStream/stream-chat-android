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

package io.getstream.chat.android.ui.feature.messages.list.options.message.internal

import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.MessageContainerMarginDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.feature.messages.list.background.MessageBackgroundFactory

internal class MessageOptionsDecoratorProvider(
    messageListItemStyle: MessageListItemStyle,
    messageReplyStyle: MessageReplyStyle,
    messageBackgroundFactory: MessageBackgroundFactory,
    showAvatarPredicate: MessageListView.ShowAvatarPredicate,
) : DecoratorProvider {

    private val messageOptionsDecorators = listOf<Decorator>(
        BackgroundDecorator(messageBackgroundFactory),
        TextDecorator(messageListItemStyle),
        MaxPossibleWidthDecorator(messageListItemStyle),
        MessageContainerMarginDecorator(messageListItemStyle),
        AvatarDecorator(showAvatarPredicate),
        ReplyDecorator(messageReplyStyle),
    )

    override val decorators: List<Decorator> = messageOptionsDecorators
}
