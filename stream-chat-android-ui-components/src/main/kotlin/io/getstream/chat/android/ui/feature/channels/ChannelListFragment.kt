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

package io.getstream.chat.android.ui.feature.channels

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelListBinding
import io.getstream.chat.android.ui.feature.channels.header.ChannelListHeaderView
import io.getstream.chat.android.ui.feature.channels.list.ChannelListFragmentViewStyle
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.messages.MessageListActivity
import io.getstream.chat.android.ui.feature.search.SearchInputView
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView
import io.getstream.chat.android.ui.utils.extensions.findListener
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModel
import io.getstream.chat.android.ui.viewmodel.search.bindView

/**
 * Self-contained channel list screen which internally contains the following components:
 *
 * - [ChannelListHeaderView] - displays information about the user and the connection state
 * - [ChannelListView] - displays a list of channel items in a paginated list
 * - [SearchInputView] - displays an input field to search message that contain specific text
 * - [SearchResultListView] - displays a list of search results
 *
 * **Note**: Fragments representing self-contained screens are easy to use. They allow you
 * to explore the SDK's features in a breeze, however, they offer limited customization.
 */
@Suppress("MemberVisibilityCanBePrivate")
public open class ChannelListFragment : Fragment() {

    protected val themeResId: Int by lazy { requireArguments().getInt(ARG_THEME_RES_ID) }
    protected val showHeader: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_HEADER, true) }
    protected val showSearch: Boolean by lazy { requireArguments().getBoolean(ARG_SHOW_SEARCH, true) }
    protected val headerTitle: String? by lazy { requireArguments().getString(ARG_HEADER_TITLE) }

    protected val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
    protected val channelListViewModel: ChannelListViewModel by viewModels { createChannelListViewModelFactory() }
    protected val searchViewModel: SearchViewModel by viewModels()

    protected lateinit var style: ChannelListFragmentViewStyle
    protected var headerUserAvatarClickListener: HeaderUserAvatarClickListener? = null
    protected var headerActionButtonClickListener: HeaderActionButtonClickListener? = null
    protected var channelListItemClickListener: ChannelListItemClickListener? = null
    protected var searchResultClickListener: SearchResultClickListener? = null

    private var _binding: StreamUiFragmentChannelListBinding? = null
    protected val binding: StreamUiFragmentChannelListBinding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.style = ChannelListFragmentViewStyle(context)
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
        val layoutInflater = if (getTheme() != 0) {
            inflater.cloneInContext(ContextThemeWrapper(context, getTheme()))
        } else {
            inflater
        }
        return StreamUiFragmentChannelListBinding.inflate(layoutInflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupChannelListHeader(binding.channelListHeaderView)
        setupChannelList(binding.channelListView)
        setupSearchInput(binding.searchInputView)
        setupSearchResultList(binding.searchResultListView)
        applyStyle(style)
    }

    protected open fun applyStyle(style: ChannelListFragmentViewStyle) {
        binding.searchInputView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = style.searchInputMarginTop
            bottomMargin = style.searchInputMarginBottom
            marginStart = style.searchInputMarginStart
            marginEnd = style.searchInputMarginEnd
        }
    }

    /**
     * Provides a custom theme for the screen.
     */
    @StyleRes
    protected open fun getTheme(): Int = themeResId

    /**
     * Configures [ChannelListHeaderView]. Override the method for a custom setup.
     *
     * @param channelListHeaderView The channel list header that is being configured.
     */
    protected open fun setupChannelListHeader(channelListHeaderView: ChannelListHeaderView) {
        with(channelListHeaderView) {
            if (showHeader) {
                channelListHeaderViewModel.bindView(this, viewLifecycleOwner)

                headerTitle?.let(::setOnlineTitle)
                setOnActionButtonClickListener {
                    headerActionButtonClickListener?.onActionButtonClick()
                }
                setOnUserAvatarClickListener {
                    headerUserAvatarClickListener?.onUserAvatarClick()
                }
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Configures [ChannelListView]. Override the method for a custom setup.
     *
     * @param channelListView The channel list that is being configured.
     */
    protected open fun setupChannelList(channelListView: ChannelListView) {
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

    /**
     * Configures [SearchInputView]. Override the method for a custom setup.
     *
     * @param searchInputView The search input that is being configured.
     */
    protected open fun setupSearchInput(searchInputView: SearchInputView) {
        with(binding.searchInputView) {
            if (showSearch) {
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
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Configures [SearchResultListView]. Override the method for a custom setup.
     *
     * @param searchResultListView The search result list that is being configured.
     */
    protected open fun setupSearchResultList(searchResultListView: SearchResultListView) {
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

    protected open fun createChannelListViewModelFactory(): ChannelListViewModelFactory = ChannelListViewModelFactory(filter = getFilter(), sort = getSort())

    /**
     * Default filter for channels. Override the method to provide custom filter.
     */
    protected open fun getFilter(): FilterObject? = null

    /**
     * Default query sort for channels. Override the method to provide custom sort.
     */
    protected open fun getSort(): QuerySorter<Channel> = ChannelListViewModel.DEFAULT_SORT

    /**
     * Click listener for the right button in the header. Not implemented by default.
     *
     * **Note**: Implement the click listener in parent Fragment or Activity
     * to override the default behavior.
     */
    public fun interface HeaderActionButtonClickListener {
        public fun onActionButtonClick()
    }

    /**
     * Click listener for the left button in the header represented by the avatar of
     * the current user. Not implemented by default.
     *
     * **Note**: Implement the click listener in parent Fragment or Activity
     * to override the default behavior.
     */
    public fun interface HeaderUserAvatarClickListener {
        public fun onUserAvatarClick()
    }

    /**
     * Click listener for channel item clicks. Navigates to [MessageListActivity] by default.
     *
     * **Note**: Implement the click listener in parent Fragment or Activity
     * to override the default behavior.
     */
    public fun interface ChannelListItemClickListener {
        public fun onChannelClick(channel: Channel)
    }

    /**
     * Click listener for search result items. Navigates to [MessageListActivity] by default.
     *
     * **Note**: Implement the click listener in parent Fragment or Activity
     * to override the default behavior.
     */
    public fun interface SearchResultClickListener {
        public fun onSearchResultClick(message: Message)
    }

    public class Builder {
        private var themeResId: Int = 0
        private var showHeader: Boolean = true
        private var showSearch: Boolean = true
        private var headerTitle: String? = null
        private var fragment: ChannelListFragment? = null

        /**
         * Custom theme for the screen.
         */
        public fun customTheme(@StyleRes themeResId: Int): Builder = apply {
            this.themeResId = themeResId
        }

        /**
         * Whether the header is shown or hidden.
         */
        public fun showHeader(showHeader: Boolean): Builder = apply {
            this.showHeader = showHeader
        }

        /**
         * Whether the search input is shown or hidden.
         */
        public fun showSearch(showSearch: Boolean): Builder = apply {
            this.showSearch = showSearch
        }

        /**
         * Header title. "Stream Chat" by default.
         */
        public fun headerTitle(headerTitle: String?): Builder = apply {
            this.headerTitle = headerTitle
        }

        /**
         * Sets custom channel list Fragment. The Fragment must be a subclass of [ChannelListFragment].
         */
        public fun <T : ChannelListFragment> setFragment(fragment: T): Builder = apply {
            this.fragment = fragment
        }

        public fun build(): ChannelListFragment = (fragment ?: ChannelListFragment()).apply {
            arguments = bundleOf(
                ARG_THEME_RES_ID to this@Builder.themeResId,
                ARG_SHOW_HEADER to this@Builder.showHeader,
                ARG_SHOW_SEARCH to this@Builder.showSearch,
                ARG_HEADER_TITLE to this@Builder.headerTitle,
            )
        }
    }

    public companion object {
        private const val ARG_THEME_RES_ID: String = "theme_res_id"
        private const val ARG_SHOW_HEADER: String = "show_header"
        private const val ARG_SHOW_SEARCH: String = "show_search"
        private const val ARG_HEADER_TITLE: String = "header_title"

        /**
         * Creates instances of [ChannelListFragment].
         *
         * @param initializer The initializer to customize builder params.
         */
        @JvmStatic
        @JvmOverloads
        public fun newInstance(initializer: (Builder.() -> Unit)? = null): ChannelListFragment {
            val builder = Builder()
            initializer?.invoke(builder)
            return builder.build()
        }
    }
}
