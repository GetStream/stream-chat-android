package io.getstream.chat.android.ui.mentions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.view.EndlessScrollListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMentionsListViewBinding
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

public class MentionsListView : ViewFlipper {

    private companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }

    private object Flipper {
        const val RESULTS = 0
        const val EMPTY = 1
        const val LOADING = 2
    }

    private val binding = StreamUiMentionsListViewBinding.inflate(LayoutInflater.from(context), this)

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    private val adapter = MentionsListAdapter(context, ChatDomain.instance())

    private var loadMoreListener: LoadMoreListener? = null

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        binding.mentionsListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = this@MentionsListView.adapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                ).apply {
                    setDrawable(context.getDrawable(R.drawable.stream_ui_divider)!!)
                }
            )
            addOnScrollListener(scrollListener)
        }
        setBackgroundColor(context.getColorCompat(R.color.stream_ui_white_snow))
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun showMessages(messages: List<Message>) {
        val isEmpty = messages.isEmpty()

        displayedChild = if (isEmpty) Flipper.EMPTY else Flipper.RESULTS

        adapter.submitList(messages)
    }

    public fun showLoading() {
        displayedChild = Flipper.LOADING
        scrollListener.disablePagination()
    }

    public fun showError() {
        Toast.makeText(context, R.string.stream_ui_mentions_list_error, Toast.LENGTH_SHORT).show()
    }

    public fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        adapter.setMentionSelectedListener(mentionSelectedListener)
    }

    public fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    public fun interface MentionSelectedListener {
        public fun onMentionSelected(message: Message)
    }

    public fun interface LoadMoreListener {
        public fun onLoadMoreRequested()
    }
}
