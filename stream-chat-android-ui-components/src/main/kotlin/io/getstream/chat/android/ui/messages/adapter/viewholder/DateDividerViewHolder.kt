package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.text.format.DateUtils
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemDateDividerBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

public class DateDividerViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    internal val binding: StreamUiItemDateDividerBinding = StreamUiItemDateDividerBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.DateSeparatorItem, diff: MessageListItemPayloadDiff?) {
        binding.dateLabel.text =
            DateUtils.getRelativeTimeSpanString(
                data.date.time,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
    }
}
