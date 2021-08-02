package io.getstream.chat.android.ui.channel.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelListBinding
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView

public open class ChannelListFragment : Fragment() {

    public var channelClickListener: ChannelClickListener? = null
    private val showHeader: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_HEADER, false) }
    private val showSearch: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_SEARCH, false) }

    private val channelListViewModel: ChannelListViewModel by viewModels {
        ChannelListViewModelFactory(filter = getFilter())
    }
    private val searchViewModel: SearchViewModel by viewModels()
    private val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()

    private var _binding: StreamUiFragmentChannelListBinding? = null
    protected val binding: StreamUiFragmentChannelListBinding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        channelClickListener = when {
            parentFragment is ChannelClickListener -> parentFragment as ChannelClickListener
            activity is ChannelClickListener -> activity as ChannelClickListener
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentChannelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        channelListViewModel.bindView(binding.channelListView, viewLifecycleOwner)
        searchViewModel.bindView(binding.searchResultListView, this)
        channelListHeaderViewModel.bindView(binding.channelListHeaderView, viewLifecycleOwner)

        binding.channelListView.setChannelItemClickListener {
            channelClickListener?.onChannelClick(it)
        }
        binding.searchInputView.apply {
            // ..
        }

        binding.channelListHeaderView.isVisible = showHeader
        binding.searchInputView.isVisible = showSearch
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        channelClickListener = null
    }

    protected open fun getFilter(): FilterObject? {
        return null
    }

    public class Builder {
        private var showHeader: Boolean = false
        private var showSearch: Boolean = false

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

    public fun interface ChannelClickListener {
        public fun onChannelClick(channel: Channel)
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
