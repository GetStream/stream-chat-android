package io.getstream.chat.sample.feature.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.view.channels.ChannelListView
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.channels.bindView
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.App
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.data.user.SampleUser
import io.getstream.chat.sample.databinding.FragmentChannelsBinding

class ChannelsFragment : Fragment() {

    private val viewModel: ChannelListViewModel by viewModels {
        ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: "")),
                Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
            ),
        )
    }

    private var _binding: FragmentChannelsBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.bindView(binding.channelsListView, viewLifecycleOwner)
        setupOnClickListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_channels)
    }

    private fun setupOnClickListeners() {
        binding.channelsListView.setChannelItemClickListener {
            findNavController().navigateSafely(ChannelsFragmentDirections.actionOpenChannel(it.cid))
        }

        binding.addNewChannelButton.setOnClickListener {
            findNavController().navigateSafely(R.id.action_to_create_channel)
        }

        binding.channelsListView.setChannelLongClickListener { channel ->
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.hide_channel_dialog)
                    .setNegativeButton(R.string.deny) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        viewModel.hideChannel(channel)
                    }.show()
            true
            }

        activity?.apply {
            onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        activity?.finish()
                    }
                }
            )
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_log_out -> {
                    App.instance.userRepository.user = SampleUser.None
                    ChatClient.instance().disconnect()
                    findNavController().navigateSafely(R.id.action_to_userLoginFragment)
                    true
                }

                R.id.mark_all_read -> {
                    viewModel.markAllRead()
                    true
                }

                else -> false
            }
        }
    }
}
