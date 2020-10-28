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
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModelImpl
import com.getstream.sdk.chat.viewmodel.channels.bindView
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.databinding.FragmentChannelsBinding

class ChannelsFragment : Fragment() {

    private val viewModel: ChannelsViewModelImpl by viewModels { ChannelsViewModelFactory() }

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

        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                if (ChannelsViewModel.State.NavigateToLoginScreen == it) {
                    findNavController().navigateSafely(R.id.action_to_usersFragment)
                }
            }
        )

        setupOnClickListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_channels)
    }

    private fun setupOnClickListeners() {
        binding.channelsListView.setOnChannelClickListener {
            findNavController().navigateSafely(ChannelsFragmentDirections.actionOpenChannel(it.cid))
        }

        binding.addNewChannelButton.setOnClickListener {
            findNavController().navigateSafely(R.id.action_to_create_channel)
        }

        binding.channelsListView.setOnLongClickListener(
            ChannelListView.ChannelClickListener { channel ->
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.hide_channel_dialog)
                    .setNegativeButton(R.string.deny) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        viewModel.hideChannel(channel)
                    }.show()
            }
        )

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
                    viewModel.onEvent(ChannelsViewModel.Event.LogoutClicked)
                    true
                }
                else -> false
            }
        }
    }
}
