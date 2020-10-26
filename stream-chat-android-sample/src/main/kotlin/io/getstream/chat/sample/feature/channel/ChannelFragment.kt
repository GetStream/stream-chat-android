package io.getstream.chat.sample.feature.channel

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.bindView
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.bindView
import io.getstream.chat.sample.R
import kotlinx.android.synthetic.main.fragment_channel.*

class ChannelFragment : Fragment(R.layout.fragment_channel) {

    private val cid: String by lazy { navArgs<ChannelFragmentArgs>().value.cid }

    private val viewModelFactory: ChannelViewModelFactory by lazy { ChannelViewModelFactory(cid) }

    private val messagesViewModel: MessageListViewModel by viewModels { viewModelFactory }

    private val channelHeaderViewModel: ChannelHeaderViewModel by viewModels { viewModelFactory }

    private val messageInputViewModel: MessageInputViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initMessagesViewModel()
        initHeaderViewModel()
        initMessageInputViewModel()

        val backButtonHandler = {
            messagesViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }

        channelHeaderView.onBackClick = backButtonHandler

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
        // ProgressBar(activity).apply {
        //     setBackgroundColor(Color.BLUE)
        //     messageListView.setLoadingView(this)
        // }
    }

    private fun initMessageInputViewModel() {
        messageInputViewModel.apply {
            bindView(messageInputView, viewLifecycleOwner)
            messagesViewModel.mode.observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        is MessageListViewModel.Mode.Thread -> setActiveThread(it.parentMessage)
                        is MessageListViewModel.Mode.Normal -> resetThread()
                    }
                }
            )
            messageListView.setOnMessageEditHandler {
                editMessage.postValue(it)
            }
        }
    }

    private fun initHeaderViewModel() {
        channelHeaderViewModel.bindView(channelHeaderView, viewLifecycleOwner)
    }

    private fun initMessagesViewModel() {
        messagesViewModel
            .apply { bindView(messageListView, viewLifecycleOwner) }
            .apply {
                state.observe(
                    viewLifecycleOwner,
                    Observer {
                        when (it) {
                            is MessageListViewModel.State.Loading -> showProgressBar()
                            is MessageListViewModel.State.Result -> hideProgressBar()
                            is MessageListViewModel.State.NavigateUp -> navigateUp()
                        }
                    }
                )
            }
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun hideProgressBar() {
        progressBar.isVisible = false
    }

    private fun showProgressBar() {
        progressBar.isVisible = true
    }
}
