package io.getstream.chat.sample.feature.create_channel
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.CreateChannelViewModel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.hideKeyboard
import io.getstream.chat.sample.common.showKeyboard
import io.getstream.chat.sample.common.showToast
import kotlinx.android.synthetic.main.fragment_new_channel.*
import org.koin.android.ext.android.inject

class CreateChannelFragment : Fragment(R.layout.fragment_new_channel) {

    private val viewModel: CreateChannelViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            submitChannelName()
        }
        channelNameInput.showKeyboard()
        channelNameInput.setOnEditorActionListener { _, _, _ ->
            submitChannelName()
            true
        }

        viewModel.state.observe(viewLifecycleOwner, Observer {
            ChatLogger.instance.logD("CreateChannel", "Received state: $it")
            when (it) {
                is CreateChannelViewModel.State.ChannelCreated -> {
                    channelNameInput.hideKeyboard()
                    findNavController().navigateUp()
                }
                is CreateChannelViewModel.State.ValidationError -> {
                    renderValidationError()
                }
                is CreateChannelViewModel.State.BackendError -> {
                    renderBackendError()
                }
            }
        })
    }

    private fun submitChannelName() {
        val channelName = channelNameInput.text.toString()
        viewModel.onEvent(CreateChannelViewModel.Event.ChannelNameSubmitted(channelName))
    }

    private fun renderValidationError() {
        channelNameInput.error = getString(R.string.create_channel_name_error)
    }

    private fun renderBackendError() {
        activity?.showToast(getString(R.string.backend_error_info))
    }

}