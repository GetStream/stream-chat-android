package io.getstream.chat.android.ui.options

import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.AvatarDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorsProvider
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GravityDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.LinkAttachmentDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator

public class MessageOptionsDecoratorsProvider : DecoratorsProvider {

    private val messageOptionsDecorators = listOf<Decorator>(
        BackgroundDecorator(),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        GravityDecorator(),
        LinkAttachmentDecorator()
    )

    override fun getDecorators(): List<Decorator> {
        return messageOptionsDecorators
    }
}
