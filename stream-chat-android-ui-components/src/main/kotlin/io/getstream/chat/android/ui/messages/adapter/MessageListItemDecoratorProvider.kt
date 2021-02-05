package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.AvatarDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.FailedIndicatorDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.FootnoteDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.LinkAttachmentDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.ReactionsDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.ReplyDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.TextDecorator
import io.getstream.chat.android.ui.messages.view.MessageListItemStyle

internal class MessageListItemDecoratorProvider(
    currentUser: User,
    dateFormatter: DateFormatter,
    isDirectMessage: Boolean,
    style: MessageListItemStyle,
) : DecoratorProvider {

    private val messageListDecorators = listOf<Decorator>(
        BackgroundDecorator(style),
        TextDecorator(style),
        GapDecorator(),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        FailedIndicatorDecorator(),
        ReactionsDecorator(),
        LinkAttachmentDecorator(),
        ReplyDecorator(currentUser),
        ReactionsDecorator(),
        FootnoteDecorator(dateFormatter, isDirectMessage),
    )

    override val decorators: List<Decorator> = messageListDecorators
}
