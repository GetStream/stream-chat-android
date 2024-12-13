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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.ui.common.utils.Utils
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModel
import io.getstream.chat.android.ui.viewmodel.search.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.databinding.FragmentChannelsBinding
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.feature.home.HomeFragmentDirections

class ChannelListFragment : Fragment() {

    private val viewModel: ChannelListViewModel by viewModels {
        val user = App.instance.userRepository.getUser()
        val userId = if (user == SampleUser.None) {
            ChatClient.instance().clientState.user.value?.id ?: ""
        } else {
            user.id
        }

        ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(userId)),
                Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
            ),
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
                .navigateSafely(HomeFragmentDirections.actionOpenChat(message.cid, message.id, message.parentId))
        }
    }

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
