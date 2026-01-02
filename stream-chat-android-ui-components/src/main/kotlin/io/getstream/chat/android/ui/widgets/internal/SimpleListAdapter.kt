/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.widgets.internal

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

internal abstract class SimpleListAdapter<T : Any, VH : SimpleListAdapter.ViewHolder<T>> : RecyclerView.Adapter<VH>() {
    var itemList: MutableList<T> = mutableListOf()

    final override fun getItemCount(): Int = itemList.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(itemList[position])
    }

    fun setItems(items: List<T>) {
        this.itemList.clear()
        this.itemList.addAll(items)
        notifyDataSetChanged()
    }

    fun removeItem(item: T) {
        val index = itemList.indexOf(item)
        if (index != -1) {
            itemList.remove(item)
            notifyItemRemoved(index)
        }
    }

    fun clear() {
        itemList.clear()
        notifyDataSetChanged()
    }

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    abstract class ViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context

        abstract fun bind(item: T)

        open fun unbind() {}
    }
}
