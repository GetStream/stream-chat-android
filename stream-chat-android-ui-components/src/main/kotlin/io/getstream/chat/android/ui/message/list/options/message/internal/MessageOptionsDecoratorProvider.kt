package io.getstream.chat.android.ui.message.list.options.message.internal

import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.LinkAttachmentDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle

internal class MessageOptionsDecoratorProvider(style: MessageListItemStyle) : DecoratorProvider {

    private val messageOptionsDecorators = listOf<Decorator>(
        BackgroundDecorator(style),
        TextDecorator(style),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        LinkAttachmentDecorator()
    )

    override val decorators: List<Decorator> = messageOptionsDecorators
}
