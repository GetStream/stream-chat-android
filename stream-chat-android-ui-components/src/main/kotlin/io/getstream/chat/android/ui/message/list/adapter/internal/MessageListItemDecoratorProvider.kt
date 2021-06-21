package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FailedIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FootnoteDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.GapDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReactionsDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator

internal class MessageListItemDecoratorProvider(
    dateFormatter: DateFormatter,
    isDirectMessage: () -> Boolean,
    messageStyle: MessageListItemStyle,
    messageReplyStyle: MessageReplyStyle
) : DecoratorProvider {

    private val messageListDecorators = listOfNotNull<Decorator>(
        BackgroundDecorator(messageStyle),
        TextDecorator(messageStyle),
        GapDecorator(),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        FailedIndicatorDecorator(),
        ReactionsDecorator(messageStyle).takeIf { messageStyle.reactionsEnabled },
        ReplyDecorator(messageReplyStyle),
        FootnoteDecorator(dateFormatter, isDirectMessage, messageStyle),
    )

    override val decorators: List<Decorator> = messageListDecorators
}
