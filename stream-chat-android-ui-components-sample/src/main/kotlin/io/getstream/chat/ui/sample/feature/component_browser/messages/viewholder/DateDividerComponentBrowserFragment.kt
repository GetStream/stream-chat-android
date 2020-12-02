package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListViewHolderBinding
import java.util.Date

class DateDividerComponentBrowserFragment : Fragment() {

    private var _binding: FragmentComponentBrowserMessageListViewHolderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComponentBrowserMessageListViewHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = DefaultAdapter(
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
