package com.getstream.sdk.chat.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.AttachmentListItemAdapter
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem

class AttachmentListView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setHasFixedSize(true)
    }

    private var viewHolderFactory: AttachmentViewHolderFactory? = null
    private var style: MessageListViewStyle? = null

    fun init(
        viewHolderFactory: AttachmentViewHolderFactory,
        style: MessageListViewStyle
    ) {
        this.viewHolderFactory = viewHolderFactory
        this.style = style
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun setEntity(messageListItem: MessageItem) {
        this.adapter = AttachmentListItemAdapter(
            messageListItem,
            checkNotNull(viewHolderFactory) { "Please call init() before using setEntity()" },
            checkNotNull(style) { "Please call init() before using setEntity()" }
        )
    }
}
