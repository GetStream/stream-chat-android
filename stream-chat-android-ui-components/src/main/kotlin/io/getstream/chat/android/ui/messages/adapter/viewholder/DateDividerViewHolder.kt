package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiItemDateDividerBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class DateDividerViewHolder(
    parent: ViewGroup,
    internal val binding: StreamUiItemDateDividerBinding = StreamUiItemDateDividerBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem>(binding.root) {

    override fun bindData(data: MessageListItem.DateSeparatorItem) {
        binding.dateLabel.text =
            DateUtils.getRelativeTimeSpanString(data.date.time, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
    }
}
