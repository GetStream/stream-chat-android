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

package io.getstream.chat.ui.sample.feature.channel.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChannelsBinding
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections

class ChannelListFragment : Fragment() {

    var isPinnedMode: Boolean = false
        set(value) {
            field = value
            viewModel.setFilters(filter)
        }

    val pinned = listOf(
        // "messaging:sample-app-channel-109",
        // "messaging:c92c5e2d-a547-4337-a3bd-2d0c29b71130",
        // "messaging:sample-app-channel-6",
        "messaging:!members-QlpghUGsUksIlxfJHiq3mkIjsbPCK7Peipy767bzjnw",
        // "messaging:!members-HmbzW3-bq4cZ76SWuXt9OFIn17cOB6HGi8i0uwCUDag",
        // "essaging:1e570561-5f5e-4606-af27-c0153adb8962",
        // "messaging:sample-app-channel-57",
        // "messaging:8d29d9ee-445a-43f6-841c-e00d91d32c42"
    )

    val filter: FilterObject
        get() = if (isPinnedMode) {
        val user = App.instance.userRepository.getUser()
        Filters.and(
            Filters.`in`("members", listOf(user.id)),
            Filters.`in`("cid", pinned),
        )
    } else {
        val user = App.instance.userRepository.getUser()
        Filters.and(
            Filters.`in`("members", listOf(user.id)),
        )
    }

    private val viewModel: ChannelListViewModel by viewModels {
        ChannelListViewModelFactory(
            filter = filter,
            chatEventHandlerFactory = CustomChatEventHandlerFactory(),
        )
    }
    private val searchViewModel: SearchViewModel by viewModels()

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchFilter.setOnCheckedChangeListener { _, isChecked ->
            isPinnedMode = isChecked
        }

        setupOnClickListeners()
        viewModel.bindView(binding.channelsView, viewLifecycleOwner)
        searchViewModel.bindView(binding.searchResultListView, this)

        binding.channelsView.apply {
            view as ViewGroup // for use as a parent in inflation

            val emptyView = layoutInflater.inflate(
                R.layout.channels_empty_view,
                view,
                false,
            )
            emptyView.findViewById<TextView>(R.id.startChatButton).setOnClickListener {
                requireActivity().findNavController(R.id.hostFragmentContainer)
                    .navigateSafely(HomeFragmentDirections.actionHomeFragmentToAddChannelFragment())
            }
            setEmptyStateView(emptyView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            setChannelItemClickListener {
                requireActivity().findNavController(R.id.hostFragmentContainer)
                    .navigateSafely(HomeFragmentDirections.actionOpenChat(it.cid))
            }

            setChannelDeleteClickListener { channel ->
                ConfirmationDialogFragment.newDeleteChannelInstance(requireContext())
                    .apply {
                        confirmClickListener =
                            ConfirmationDialogFragment.ConfirmClickListener { viewModel.deleteChannel(channel) }
                    }
                    .show(parentFragmentManager, null)
            }

            setChannelInfoClickListener { channel ->
                val direction = when {
                    channel.members.size > 2 || channel.isAnonymousChannel() ->
                        HomeFragmentDirections.actionHomeFragmentToGroupChatInfoFragment(channel.cid)

                    else -> HomeFragmentDirections.actionHomeFragmentToChatInfoFragment(channel.cid)
                }

                requireActivity()
                    .findNavController(R.id.hostFragmentContainer)
                    .navigateSafely(direction)
            }
        }

        binding.searchInputView.apply {
            setDebouncedInputChangedListener { query ->
                if (query.isEmpty()) {
                    binding.channelsView.isVisible = true
                    binding.searchResultListView.isVisible = false
                }
            }
            setSearchStartedListener { query ->
                Utils.hideSoftKeyboard(binding.searchInputView)
                searchViewModel.setQuery(query)
                binding.channelsView.isVisible = query.isEmpty()
                binding.searchResultListView.isVisible = query.isNotEmpty()
            }
        }

        binding.searchResultListView.setSearchResultSelectedListener { message ->
            requireActivity().findNavController(R.id.hostFragmentContainer)
                .navigateSafely(HomeFragmentDirections.actionOpenChat(message.cid, message.id))
        }
    }

    @OptIn(InternalStreamChatApi::class)
    private fun setupOnClickListeners() {
        activity?.apply {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if (binding.searchInputView.clear()) {
                    return@addCallback
                }

                finish()
            }
        }
    }
}
