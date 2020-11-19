package io.getstream.chat.android.ui.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamSearchResultListViewBinding

public class SearchResultListView : LinearLayout {

    private val binding = StreamSearchResultListViewBinding.inflate(LayoutInflater.from(context), this)

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private val adapter = SearchResultListAdapter()

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        orientation = VERTICAL

        binding.searchListView.apply {
            setHasFixedSize(true)
            adapter = this@SearchResultListView.adapter
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun setMessages(messages: List<Message>) {
        val count = messages.count()
        binding.searchInfoBar.text = when (count) {
            0 -> resources.getString(R.string.stream_search_result_list_result_count_empty)
            else -> resources.getQuantityString(R.plurals.stream_search_result_list_result_count, count, count)
        }
        adapter.submitList(messages)
    }

    public fun setSearchResultSelectedListener(searchResultSelectedListener: SearchResultSelectedListener?) {
        adapter.setSearchResultSelectedListener(searchResultSelectedListener)
    }

    public fun interface SearchResultSelectedListener {
        public fun onSearchResultSelected(message: Message)
    }
}
