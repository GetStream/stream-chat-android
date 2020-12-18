package com.getstream.sdk.chat.adapter.viewholder.message

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import com.getstream.sdk.chat.databinding.StreamItemDateSeparatorBinding
import com.getstream.sdk.chat.enums.Dates
import com.getstream.sdk.chat.enums.label
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.MessageListViewStyle
import java.util.Date

internal class DateSeparatorViewHolder(
    parent: ViewGroup,
    private val style: MessageListViewStyle,
    private val binding: StreamItemDateSeparatorBinding =
        StreamItemDateSeparatorBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<DateSeparatorItem>(binding.root) {

    override fun bind(messageListItem: DateSeparatorItem, diff: MessageListItemPayloadDiff) {
        configDate(messageListItem)
        applyStyle()
    }

    private fun configDate(messageListItem: DateSeparatorItem) {
        val messageDate = messageListItem.date.time
        val now = Date()
        val humanizedDate =
            if (now.time - messageDate < 60 * 1000) {
                Dates.JUST_NOW.label
            } else {
                DateUtils.getRelativeTimeSpanString(messageDate)
            }
        binding.tvDate.text = humanizedDate
    }

    private fun applyStyle() {
        style.dateSeparatorDateText.apply(binding.tvDate)

        if (style.dateSeparatorLineDrawable != -1) {
            val drawable = style.dateSeparatorLineDrawable
            binding.ivLineRight.background = ContextCompat.getDrawable(context, drawable)
            binding.ivLineLeft.background = ContextCompat.getDrawable(context, drawable)
        } else {
            binding.ivLineRight.setBackgroundColor(style.dateSeparatorLineColor)
            binding.ivLineLeft.setBackgroundColor(style.dateSeparatorLineColor)
        }
        configSeparatorLineWidth(binding.ivLineRight)
        configSeparatorLineWidth(binding.ivLineLeft)
    }

    private fun configSeparatorLineWidth(view: View) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = style.dateSeparatorLineWidth
        }
    }
}
