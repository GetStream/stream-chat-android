package io.getstream.chat.android.ui.channel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.common.extensions.internal.findListener
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelListBinding
import io.getstream.chat.android.ui.message.MessageListActivity
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView

/**
 * Self-contained channel list screen which internally contains the following components:
 *
 * - [ChannelListHeaderView] - displays information about the user and the connection state
 * - [ChannelListView] - displays a list of channel items in a paginated list
 * - [SearchInputView] - displays an input field to search message that contain specific text
 * - [SearchResultListView] - displays a list of search results
 *
 * **Note**: Fragments representing Self-contained screens are easy to use. They allow you
 * to explore the SDK's features in a breeze, however, they offer limited customization.
 */
public open class ChannelListFragment : Fragment() {

    private val showHeader: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_HEADER, true) }
    private val showSearch: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_SEARCH, true) }

    private val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
    private val channelListViewModel: ChannelListViewModel by viewModels { createChannelListViewModelFactory() }
    private val searchViewModel: SearchViewModel by viewModels()

    private var headerUserAvatarClickListener: HeaderUserAvatarClickListener? = null
    private var headerActionButtonClickListener: HeaderActionButtonClickListener? = null
    private var channelListItemClickListener: ChannelListItemClickListener? = null
    private var searchResultClickListener: SearchResultClickListener? = null

    private var _binding: StreamUiFragmentChannelListBinding? = null
    protected val binding: StreamUiFragmentChannelListBinding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        headerUserAvatarClickListener = findListener()
        headerActionButtonClickListener = findListener()
        channelListItemClickListener = findListener()
        searchResultClickListener = findListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return StreamUiFragmentChannelListBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupChannelListHeader()
        setupChannelList()
        setupSearchInput()
        setupSearchResultList()
    }

    protected open fun setupChannelListHeader() {
        with(binding.channelListHeaderView) {
            channelListHeaderViewModel.bindView(this, viewLifecycleOwner)

            setOnActionButtonClickListener {
                headerActionButtonClickListener?.onActionButtonClick()
            }
            setOnUserAvatarClickListener {
                headerUserAvatarClickListener?.onUserAvatarClick()
            }
            isVisible = showHeader
        }
    }

    protected open fun setupChannelList() {
        with(binding.channelListView) {
            channelListViewModel.bindView(this, viewLifecycleOwner)

            setChannelItemClickListener {
                if (channelListItemClickListener == null) {
                    startActivity(MessageListActivity.createIntent(requireContext(), it.cid))
                } else {
                    channelListItemClickListener?.onChannelClick(it)
                }
            }
        }
    }

    protected open fun setupSearchInput() {
        with(binding.searchInputView) {
            setDebouncedInputChangedListener { query ->
                if (query.isEmpty()) {
                    binding.channelListView.isVisible = true
                    binding.searchResultListView.isVisible = false
                }
            }
            setSearchStartedListener { query ->
                Utils.hideSoftKeyboard(this)
                searchViewModel.setQuery(query)
                binding.channelListView.isVisible = query.isEmpty()
                binding.searchResultListView.isVisible = query.isNotEmpty()
            }
            isVisible = showSearch
        }
    }

    protected open fun setupSearchResultList() {
        with(binding.searchResultListView) {
            searchViewModel.bindView(this, viewLifecycleOwner)

            setSearchResultSelectedListener {
                if (searchResultClickListener == null) {
                    startActivity(MessageListActivity.createIntent(requireContext(), it.cid, it.id))
                } else {
                    searchResultClickListener?.onSearchResultClick(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        headerUserAvatarClickListener = null
        headerActionButtonClickListener = null
        channelListItemClickListener = null
        searchResultClickListener = null
    }

    protected open fun createChannelListViewModelFactory(): ChannelListViewModelFactory {
        return ChannelListViewModelFactory(filter = getFilter(), sort = getSort())
    }

    protected open fun getFilter(): FilterObject? {
        return null
    }

    protected open fun getSort(): QuerySort<Channel> {
        return ChannelListViewModel.DEFAULT_SORT
    }

    public fun interface HeaderActionButtonClickListener {
        public fun onActionButtonClick()
    }

    public fun interface HeaderUserAvatarClickListener {
        public fun onUserAvatarClick()
    }

    public fun interface ChannelListItemClickListener {
        public fun onChannelClick(channel: Channel)
    }

    public fun interface SearchResultClickListener {
        public fun onSearchResultClick(message: Message)
    }

    public class Builder {
        private var showHeader: Boolean = true
        private var showSearch: Boolean = true

        private var fragment: ChannelListFragment? = null

        public fun showHeader(showHeader: Boolean): Builder = apply {
            this.showHeader = showHeader
        }

        public fun showSearch(showSearch: Boolean): Builder = apply {
            this.showSearch = showSearch
        }

        public fun <T : ChannelListFragment> setFragment(fragment: T): Builder = apply {
            this.fragment = fragment
        }

        public fun build(): ChannelListFragment {
            return (fragment ?: ChannelListFragment()).apply {
                arguments = bundleOf(
                    ARG_SHOW_HEADER to this@Builder.showHeader,
                    ARG_SHOW_SEARCH to this@Builder.showSearch
                )
            }
        }
    }

    public companion object {
        public const val ARG_SHOW_HEADER: String = "show_header"
        public const val ARG_SHOW_SEARCH: String = "show_search"

        public fun newInstance(initializer: Builder.() -> Unit): ChannelListFragment {
            val builder = Builder()
            builder.initializer()
            return builder.build()
        }
    }
}
