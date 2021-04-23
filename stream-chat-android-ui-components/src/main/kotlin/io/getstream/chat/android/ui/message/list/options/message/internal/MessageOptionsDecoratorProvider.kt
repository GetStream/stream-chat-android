package io.getstream.chat.android.ui.message.list.options.message.internal

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator

internal class MessageOptionsDecoratorProvider(style: MessageListItemStyle, currentUser: User) : DecoratorProvider {

    private val messageOptionsDecorators = listOf<Decorator>(
        BackgroundDecorator(style),
        TextDecorator(style),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        ReplyDecorator { currentUser },
    )

    override val decorators: List<Decorator> = messageOptionsDecorators
}
