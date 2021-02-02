package io.getstream.chat.sample.feature.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.view.bindView
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.bindView
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.sample.databinding.FragmentChannelBinding

class ChannelFragment : Fragment() {

    private val cid: String by lazy { navArgs<ChannelFragmentArgs>().value.cid }

    private val viewModelFactory: ChannelViewModelFactory by lazy { ChannelViewModelFactory(cid) }

    private val messagesViewModel: MessageListViewModel by viewModels { viewModelFactory }

    private val channelHeaderViewModel: ChannelHeaderViewModel by viewModels { viewModelFactory }

    private val messageInputViewModel: MessageInputViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentChannelBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initMessagesViewModel()
        initHeaderViewModel()
        initMessageInputViewModel()

        val backButtonHandler = {
            messagesViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }

        binding.channelHeaderView.onBackClick = backButtonHandler

        activity?.apply {
            onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backButtonHandler()
                    }
                }
            )
        }
    }

    private fun initMessageInputViewModel() {
        messageInputViewModel.apply {
            bindView(binding.messageInputView, viewLifecycleOwner)
            messagesViewModel.mode.observe(viewLifecycleOwner) {
                when (it) {
                    is MessageListViewModel.Mode.Thread -> setActiveThread(it.parentMessage)
                    is MessageListViewModel.Mode.Normal -> resetThread()
                }
            }
            binding.messageListView.setOnMessageEditHandler {
                editMessage.postValue(it)
            }
        }
    }

    private fun initHeaderViewModel() {
        channelHeaderViewModel.bindView(binding.channelHeaderView, viewLifecycleOwner)
    }

    private fun initMessagesViewModel() {
        messagesViewModel
            .apply { bindView(binding.messageListView, viewLifecycleOwner) }
            .apply {
                state.observe(viewLifecycleOwner) {
                    when (it) {
                        is MessageListViewModel.State.Loading -> showProgressBar()
                        is MessageListViewModel.State.Result -> hideProgressBar()
                        is MessageListViewModel.State.NavigateUp -> navigateUp()
                    }
                }
            }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun hideProgressBar() {
        binding.progressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding.progressBar.isVisible = true
    }
}
