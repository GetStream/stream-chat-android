package io.getstream.chat.sample.feature.channels

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModelImpl
import com.getstream.sdk.chat.viewmodel.channels.bindView
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.navigateSafely
import kotlinx.android.synthetic.main.fragment_channels.*

class ChannelsFragment : Fragment(R.layout.fragment_channels) {
    private val viewModel by lazy { ChannelsViewModelImpl() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.bindView(channelsListView, this@ChannelsFragment)

        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                if (ChannelsViewModel.State.NavigateToLoginScreen == it) {
                    findNavController().navigateSafely(R.id.action_to_loginFragment)
                }
            }
        )

        setupOnClickListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.menu_channels)
    }

    private fun setupOnClickListeners() {
        channelsListView.setOnChannelClickListener {
            findNavController().navigateSafely(ChannelsFragmentDirections.actionOpenChannel(it.cid))
        }

        addNewChannelButton.setOnClickListener {
            findNavController().navigateSafely(R.id.action_to_create_channel)
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

        toolbar.setOnMenuItemClickListener {
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
