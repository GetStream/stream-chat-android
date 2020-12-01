package io.getstream.chat.android.ui.mentions

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message

public class MentionsListView : RecyclerView {

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

    private val adapter = MentionsListAdapter()

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)
        setHasFixedSize(true)
        setAdapter(adapter)
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

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun setMessages(messages: List<Message>) {
        adapter.submitList(messages)
    }

    public fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        adapter.setMentionSelectedListener(mentionSelectedListener)
    }

    public fun interface MentionSelectedListener {
        public fun onMentionSelected(message: Message)
    }
}
