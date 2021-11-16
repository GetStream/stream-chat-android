package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.AvatarDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FailedIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.FootnoteDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.GapDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.MessageContainerMarginDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.PinIndicatorDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReactionsDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.ReplyDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.TextDecorator
import io.getstream.chat.android.ui.message.list.background.MessageBackgroundFactory

internal class MessageListItemDecoratorProvider(
    dateFormatter: DateFormatter,
    isDirectMessage: () -> Boolean,
    messageListViewStyle: MessageListViewStyle,
    showAvatarPredicate: MessageListView.ShowAvatarPredicate,
    messageBackgroundFactory: MessageBackgroundFactory
) : DecoratorProvider {

    private val messageListDecorators = listOfNotNull<Decorator>(
        BackgroundDecorator(messageBackgroundFactory),
        TextDecorator(messageListViewStyle.itemStyle),
        GapDecorator(),
        MaxPossibleWidthDecorator(messageListViewStyle.itemStyle),
        MessageContainerMarginDecorator(messageListViewStyle.itemStyle),
        AvatarDecorator(showAvatarPredicate),
        FailedIndicatorDecorator(),
        ReactionsDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.reactionsEnabled },
        ReplyDecorator(messageListViewStyle.replyMessageStyle),
        FootnoteDecorator(dateFormatter, isDirectMessage, messageListViewStyle),
        PinIndicatorDecorator(messageListViewStyle.itemStyle).takeIf { messageListViewStyle.pinMessageEnabled },
    )

    override val decorators: List<Decorator> = messageListDecorators
}
