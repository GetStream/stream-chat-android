package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.text.format.DateUtils
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import java.util.Date

class DateDividerComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun createAdapter(): RecyclerView.Adapter<*> {
        return DefaultAdapter(
            getDummyDateDividerList(),
            ::DateDividerViewHolder,
            DateDividerViewHolder::bind
        )
    }

    private fun getDummyDateDividerList(): List<MessageListItem.DateSeparatorItem> {
        return listOf(
            MessageListItem.DateSeparatorItem(Date()),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 2 * DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 6 * DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 7 * DateUtils.DAY_IN_MILLIS)),
        )
    }
}
