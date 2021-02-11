package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FailedIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FootnoteDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.GapDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.LinkAttachmentDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReactionsDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.message.list.internal.MessageListItemStyle

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
