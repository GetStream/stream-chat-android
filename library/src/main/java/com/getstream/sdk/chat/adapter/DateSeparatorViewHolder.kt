package com.getstream.sdk.chat.adapter

import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.enums.Dates
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel
import java.util.Date

class DateSeparatorViewHolder(
    resId: Int,
    viewGroup: ViewGroup,
    private val style: MessageListViewStyle
) : BaseMessageListItemViewHolder<DateSeparatorItem>(resId, viewGroup) {

    private val tv_date: TextView = itemView.findViewById(R.id.tv_date)
    private val iv_line_right: ImageView = itemView.findViewById(R.id.iv_line_right)
    private val iv_line_left: ImageView = itemView.findViewById(R.id.iv_line_left)

    private var messageListItem: DateSeparatorItem? = null

    override fun bind(
        channel: Channel,
        messageListItem: DateSeparatorItem,
        position: Int
    ) {
        this.messageListItem = messageListItem
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
        tv_date.text = humanizedDate
    }

    private fun applyStyle() {
        style.dateSeparatorDateText.apply(tv_date)

        if (style.dateSeparatorLineDrawable != -1) {
            val drawable = style.dateSeparatorLineDrawable
            iv_line_right.background = ContextCompat.getDrawable(context, drawable)
            iv_line_left.background = ContextCompat.getDrawable(context, drawable)
        } else {
            iv_line_right.setBackgroundColor(style.dateSeparatorLineColor)
            iv_line_left.setBackgroundColor(style.dateSeparatorLineColor)
        }
        configSeparatorLineWidth(iv_line_right)
        configSeparatorLineWidth(iv_line_left)
    }

    private fun configSeparatorLineWidth(view: View) {
        view.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = style.dateSeparatorLineWidth
        }
    }
}
