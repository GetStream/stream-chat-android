package io.getstream.chat.sample.feature.create_channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.hideKeyboard
import io.getstream.chat.sample.common.showKeyboard
import io.getstream.chat.sample.common.showToast
import io.getstream.chat.sample.databinding.FragmentNewChannelBinding

class CreateChannelFragment : Fragment() {

    private var _binding: FragmentNewChannelBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.channelNameInput.showKeyboard()
        binding.toolbar.setNavigationOnClickListener { goBack() }

        val viewModel = CreateChannelViewModel()
        binding.submitButton.setOnClickListener {
            val channelName = binding.channelNameInput.text.toString()
            viewModel.onEvent(CreateChannelViewModel.Event.ChannelNameSubmitted(channelName))
        }
        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is CreateChannelViewModel.State.ChannelCreated -> { goBack() }
                    is CreateChannelViewModel.State.ValidationError -> { renderValidationError() }
                    is CreateChannelViewModel.State.BackendError -> { renderBackendError() }
                    is CreateChannelViewModel.State.Loading -> { binding.progressBar.isVisible = true }
                }
            }
        )
    }

    private fun goBack() {
        binding.channelNameInput.hideKeyboard()
        findNavController().navigateUp()
    }

    private fun renderValidationError() {
        binding.channelNameInput.error = getString(R.string.create_channel_name_error)
    }

    private fun renderBackendError() {
        activity?.showToast(getString(R.string.backend_error_info))
    }
}
