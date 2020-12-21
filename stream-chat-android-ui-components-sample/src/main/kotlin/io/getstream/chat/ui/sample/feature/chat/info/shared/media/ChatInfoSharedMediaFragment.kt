package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.view.EndlessScrollListener
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedMediaBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModel
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ChatInfoSharedMediaFragment : Fragment() {

    private val args: ChatInfoSharedMediaFragmentArgs by navArgs()
    private val factory: ChatInfoSharedAttachmentsViewModelFactory by lazy {
        ChatInfoSharedAttachmentsViewModelFactory(
            args.cid,
            ChatInfoSharedAttachmentsViewModel.AttachmentsType.MEDIA
        )
    }
    private val viewModel: ChatInfoSharedAttachmentsViewModel by viewModels { factory }
    private val sharedMediaViewModel: ChatInfoSharedMediaViewModel by activityViewModels()
    private val adapter: ChatInfoSharedMediaAdapter = ChatInfoSharedMediaAdapter()
    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        viewModel.onAction(ChatInfoSharedAttachmentsViewModel.Action.LoadMoreRequested)
    }
    private val dateScrollListener by lazy {
        MediaDateScrollListener(
            spanCount = SPAN_COUNT,
            positionChangeThreshold = { binding.mediaRecyclerView.top - binding.dateTextView.bottom }
        ) { binding.dateTextView.text = getDateText(adapter.currentList) }
    }
    private val dateFormat: DateFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    private var _binding: FragmentChatInfoSharedMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatInfoSharedMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        binding.mediaRecyclerView.apply {
            adapter = this@ChatInfoSharedMediaFragment.adapter
            addItemDecoration(SharedMediaSpaceItemDecorator())
            addOnScrollListener(scrollListener)
            addOnScrollListener(dateScrollListener)
        }
        adapter.setMediaClickListener {
            sharedMediaViewModel.attachmentWithDate.value =
                adapter.currentList.map { attachmentItem -> attachmentItem.attachmentWithDate }
            findNavController().navigate(R.id.action_chatInfoSharedMediaFragment_to_chatInfoSharedMediaGalleryFragment)
        }
        bindViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                showLoading()
            } else {
                if (state.results.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(state.results.filterIsInstance<SharedAttachment.AttachmentItem>())
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.mediaRecyclerView.isVisible = false
        binding.dateView.isVisible = false
        binding.emptyStateView.isVisible = false
        scrollListener.disablePagination()
    }

    private fun showResults(attachments: List<SharedAttachment.AttachmentItem>) {
        binding.progressBar.isVisible = false
        binding.mediaRecyclerView.isVisible = true
        binding.dateView.isVisible = true
        binding.emptyStateView.isVisible = false
        scrollListener.enablePagination()
        adapter.submitList(attachments)
        binding.dateTextView.text = getDateText(attachments)
    }

    private fun getDateText(attachments: List<SharedAttachment.AttachmentItem>): String {
        return dateFormat.format(attachments[dateScrollListener.lastVisibleItemPosition].attachmentWithDate.createdAt)
    }

    private fun showEmptyState() {
        binding.progressBar.isVisible = false
        binding.mediaRecyclerView.isVisible = false
        binding.dateView.isVisible = false
        binding.emptyStateView.isVisible = true
        scrollListener.disablePagination()
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

    companion object {
        private const val LOAD_MORE_THRESHOLD = 10
        private const val SPAN_COUNT = 3
        private val MEDIA_ITEM_SPACE = Utils.dpToPx(2)
    }
}
