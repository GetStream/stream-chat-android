package io.getstream.chat.android.ui.message.list.options.message.internal

import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MessageContainerMarginDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory

internal class MessageOptionsDecoratorProvider(
    messageListItemStyle: MessageListItemStyle,
    messageReplyStyle: MessageReplyStyle,
    messageBackgroundFactory: MessageBackgroundFactory
) : DecoratorProvider {

    private val messageOptionsDecorators = listOf<Decorator>(
        BackgroundDecorator(messageBackgroundFactory),
        TextDecorator(messageListItemStyle),
        MaxPossibleWidthDecorator(messageListItemStyle),
        MessageContainerMarginDecorator(messageListItemStyle),
        AvatarDecorator(),
        ReplyDecorator(messageReplyStyle),
    )

    override val decorators: List<Decorator> = messageOptionsDecorators
}
