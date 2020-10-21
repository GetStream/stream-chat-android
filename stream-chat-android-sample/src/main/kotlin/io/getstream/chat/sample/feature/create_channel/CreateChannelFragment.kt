package io.getstream.chat.sample.feature.create_channel

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.CreateChannelViewModel
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.hideKeyboard
import io.getstream.chat.sample.common.showKeyboard
import io.getstream.chat.sample.common.showToast
import kotlinx.android.synthetic.main.fragment_new_channel.*

class CreateChannelFragment : Fragment(R.layout.fragment_new_channel) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        channelNameInput.showKeyboard()
        toolbar.setNavigationOnClickListener { goBack() }

        val viewModel = CreateChannelViewModel()
        submitButton.setOnClickListener {
            val channelName = channelNameInput.text.toString()
            viewModel.onEvent(CreateChannelViewModel.Event.ChannelNameSubmitted(channelName))
        }
        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is CreateChannelViewModel.State.ChannelCreated -> { goBack() }
                    is CreateChannelViewModel.State.ValidationError -> { renderValidationError() }
                    is CreateChannelViewModel.State.BackendError -> { renderBackendError() }
                    is CreateChannelViewModel.State.Loading -> { progressBar.isVisible = true }
                }
            }
        )
    }

    private fun goBack() {
        channelNameInput.hideKeyboard()
        findNavController().navigateUp()
    }

    private fun renderValidationError() {
        channelNameInput.error = getString(R.string.create_channel_name_error)
    }

    private fun renderBackendError() {
        activity?.showToast(getString(R.string.backend_error_info))
    }
}
