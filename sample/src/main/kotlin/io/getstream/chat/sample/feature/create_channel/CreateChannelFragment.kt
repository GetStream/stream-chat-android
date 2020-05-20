package io.getstream.chat.sample.feature.create_channel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.CreateChannelViewModel
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.hideKeyboard
import kotlinx.android.synthetic.main.fragment_new_channel.*
import org.koin.android.ext.android.inject

class CreateChannelFragment : Fragment(R.layout.fragment_new_channel) {

    private val viewModel: CreateChannelViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            val channelName = channelNameInput.text.toString()
            viewModel.onEvent(CreateChannelViewModel.Event.ChannelNameSubmitted(channelName))
        }

        viewModel.state.observe(viewLifecycleOwner, Observer {
            if (it is CreateChannelViewModel.State.ChannelCreated) {
                channelNameInput.hideKeyboard()
                findNavController().navigateUp()
            } else if (it is CreateChannelViewModel.State.Error) {
                renderError(it)
            }
        })
    }

    private fun renderError(error: CreateChannelViewModel.State.Error) {
        channelNameInput.error = getString(R.string.create_channel_name_error)
    }

}