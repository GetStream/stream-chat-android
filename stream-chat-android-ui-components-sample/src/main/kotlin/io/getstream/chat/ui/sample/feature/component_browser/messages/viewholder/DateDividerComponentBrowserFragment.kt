package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.text.format.DateUtils
import com.getstream.sdk.chat.adapter.MessageListItem
import java.util.Date

class DateDividerComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun getItems(): List<MessageListItem> {
        return listOf(
            MessageListItem.DateSeparatorItem(Date()),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 2 * DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 6 * DateUtils.DAY_IN_MILLIS)),
            MessageListItem.DateSeparatorItem(Date(System.currentTimeMillis() - 7 * DateUtils.DAY_IN_MILLIS)),
        )
    }
}
