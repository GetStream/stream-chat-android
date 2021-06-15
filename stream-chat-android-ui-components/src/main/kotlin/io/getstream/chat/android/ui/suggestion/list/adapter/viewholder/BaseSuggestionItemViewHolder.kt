package io.getstream.chat.android.ui.suggestion.list.adapter.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem

public abstract class BaseSuggestionItemViewHolder<T : SuggestionListItem>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    protected val context: Context
        get() = itemView.context

    public abstract fun bindItem(item: T)
}
