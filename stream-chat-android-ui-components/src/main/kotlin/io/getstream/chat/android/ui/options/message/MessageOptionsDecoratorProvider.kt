package io.getstream.chat.android.ui.options.message

import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.AvatarDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.LinkAttachmentDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.TextDecorator
import io.getstream.chat.android.ui.messages.view.MessageListItemStyle

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
