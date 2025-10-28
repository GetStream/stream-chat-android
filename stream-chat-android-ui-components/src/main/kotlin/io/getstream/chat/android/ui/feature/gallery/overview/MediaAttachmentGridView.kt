/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.gallery.overview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.databinding.StreamUiMediaAttachmentGridViewBinding
import io.getstream.chat.android.ui.feature.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.feature.gallery.MediaAttachmentGridViewStyle
import io.getstream.chat.android.ui.feature.gallery.overview.internal.MediaAttachmentAdapter
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.EndlessScrollListener
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

public class MediaAttachmentGridView : FrameLayout {

    private val binding = StreamUiMediaAttachmentGridViewBinding.inflate(streamThemeInflater, this, true)
    private val dateFormat: DateFormat = SimpleDateFormat("MMM yyyy", Locale.US)
    private var showUserAvatars: Boolean = false

    /**
     * Style used to change the appearance of the view
     */
    private lateinit var style: MediaAttachmentGridViewStyle

    private val adapter: MediaAttachmentAdapter by lazy {
        MediaAttachmentAdapter(style = style) {
            mediaClickListener?.onClick(it)
        }
    }

    private val dateScrollListener by lazy {
        MediaDateScrollListener(
            spanCount = SPAN_COUNT,
            positionChangeThreshold = { binding.mediaRecyclerView.top - binding.dateTextView.bottom },
        ) {
            setDateText(adapter.currentList)
        }
    }
    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMore()
    }

    private var mediaClickListener: MediaClickListener? = null
    private var loadMoreListener: OnLoadMoreListener? = null

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        style = MediaAttachmentGridViewStyle(
            context = context,
            attrs = attrs,
        )

        showUserAvatars = style.showUserAvatars

        binding.mediaRecyclerView.apply {
            adapter = this@MediaAttachmentGridView.adapter
            addOnScrollListener(scrollListener)
            addOnScrollListener(dateScrollListener)
        }
    }

    public fun setAttachments(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        adapter.submitList(attachmentGalleryItems)
        setDateText(attachmentGalleryItems)
    }

    public fun disablePagination() {
        scrollListener.disablePagination()
    }

    public fun enablePagination() {
        scrollListener.enablePagination()
    }

    public fun setMediaClickListener(listener: MediaClickListener?) {
        mediaClickListener = listener
    }

    public fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
        loadMoreListener = listener
    }

    public fun getAttachments(): List<AttachmentGalleryItem> = adapter.currentList

    private fun setDateText(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        val createdAt = when (attachmentGalleryItems.isNotEmpty()) {
            true -> attachmentGalleryItems[dateScrollListener.lastVisibleItemPosition].createdAt
            else -> null
        }
        binding.dateContainer.isVisible = attachmentGalleryItems.isNotEmpty()
        binding.dateTextView.text = createdAt?.let { dateFormat.format(it) }
    }

    private class MediaDateScrollListener(
        private val spanCount: Int,
        private val positionChangeThreshold: () -> Int,
        private val onVisibleItemChanged: () -> Unit,
    ) : RecyclerView.OnScrollListener() {
        var lastVisibleItemPosition = 0
            private set

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val adapter = requireNotNull(recyclerView.adapter)
            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val visibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            layoutManager.findViewByPosition(visibleItemPosition)?.let { view ->
                val actualPosition = if (view.bottom < positionChangeThreshold()) {
                    // Taking first element from next row
                    val lastRowElementsCount = adapter.itemCount % spanCount
                    val lastRowFirstElement = if (lastRowElementsCount == 0) {
                        adapter.itemCount - 1
                    } else {
                        adapter.itemCount - lastRowElementsCount
                    }
                    minOf(visibleItemPosition + spanCount, lastRowFirstElement)
                } else {
                    visibleItemPosition
                }

                if (lastVisibleItemPosition != actualPosition) {
                    lastVisibleItemPosition = actualPosition
                    onVisibleItemChanged()
                }
            }
        }
    }

    public fun interface MediaClickListener {
        public fun onClick(position: Int)
    }

    public fun interface OnLoadMoreListener {
        public fun onLoadMore()
    }

    // TODO: leaves empty space after pagination
    private class SharedMediaSpaceItemDecorator : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            parent.adapter?.let { adapter ->
                // Add spaces between elements
                when {
                    parent.getChildAdapterPosition(view) % SPAN_COUNT == 0 -> {
                        outRect.top = MEDIA_ITEM_SPACE
                        outRect.right = MEDIA_ITEM_SPACE / 2
                    }
                    parent.getChildAdapterPosition(view) % SPAN_COUNT == SPAN_COUNT - 1 -> {
                        outRect.top = MEDIA_ITEM_SPACE
                        outRect.left = MEDIA_ITEM_SPACE / 2
                    }
                    else -> {
                        outRect.top = MEDIA_ITEM_SPACE
                        outRect.right = MEDIA_ITEM_SPACE / 4
                        outRect.left = MEDIA_ITEM_SPACE / 4
                    }
                }

                val lastRowCount = if (adapter.itemCount % SPAN_COUNT != 0) {
                    adapter.itemCount % SPAN_COUNT
                } else {
                    SPAN_COUNT
                }
                // Add additional bottom margin for last row to enable scrolling to the top
                if (parent.getChildAdapterPosition(view) >= adapter.itemCount - lastRowCount) {
                    outRect.bottom = parent.height - (parent.width / SPAN_COUNT)
                }
            }
        }
    }

    public companion object {
        private const val LOAD_MORE_THRESHOLD = 10
        private const val SPAN_COUNT = 3
        private val MEDIA_ITEM_SPACE = Utils.dpToPx(2)
    }
}
