package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView

internal val RecyclerView.ViewHolder.context: Context
    get() = itemView.context

internal val RecyclerView.ViewHolder.inflater: LayoutInflater
    get() = LayoutInflater.from(context)
