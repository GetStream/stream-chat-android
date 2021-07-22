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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
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
import io.getstream.chat.android.ui.channel.list.viewmodel.v2.ChannelListViewModel as ChannelListViewModelV2
import io.getstream.chat.android.ui.channel.list.viewmodel.v2.bindView as bindViewV2
import io.getstream.chat.android.ui.channel.list.viewmodel.v2.factory.ChannelListViewModelFactory as ChannelListViewModelFactoryV2

class ChannelListFragment : Fragment() {

    private val viewModelV2: ChannelListViewModelV2 by viewModels { ChannelListViewModelFactoryV2() }
    private val viewModel: ChannelListViewModel by viewModels {
        ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: "")),
                Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
            ),
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
        bindView()
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
                            ConfirmationDialogFragment.ConfirmClickListener { deleteChannel(channel) }
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

    private fun deleteChannel(channel: Channel) {
        if (App.instance.offlinePluginEnabled) {
            viewModelV2.deleteChannel(channel)
        } else {
            viewModel.deleteChannel(channel)
        }
    }

    private fun bindView() {
        if (App.instance.offlinePluginEnabled) {
            viewModelV2.bindViewV2(binding.channelsView, viewLifecycleOwner)
        } else {
            viewModel.bindView(binding.channelsView, viewLifecycleOwner)
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
