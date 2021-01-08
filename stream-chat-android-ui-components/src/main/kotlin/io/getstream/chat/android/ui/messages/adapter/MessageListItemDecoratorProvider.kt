package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.AvatarDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.BackgroundDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DeliveryStatusDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.FailedIndicatorDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GapDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.GravityDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.LinkAttachmentDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MaxPossibleWidthDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.MessageFooterDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.ReactionsDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.ReplyDecorator
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.ThreadRepliesDecorator

public class MessageListItemDecoratorProvider(
    currentUser: User,
    dateFormatter: DateFormatter,
    directMessage: Boolean,
) : DecoratorProvider {

    private val messageListDecorators = listOf<Decorator>(
        BackgroundDecorator(),
        GapDecorator(),
        MaxPossibleWidthDecorator(),
        AvatarDecorator(),
        GravityDecorator(),
        DeliveryStatusDecorator(),
        FailedIndicatorDecorator(),
        MessageFooterDecorator(dateFormatter, directMessage),
        ReactionsDecorator(),
        LinkAttachmentDecorator(),
        ReplyDecorator(currentUser),
        ReactionsDecorator(),
        ThreadRepliesDecorator(),
    )

    override val decorators: List<Decorator> = messageListDecorators
}
