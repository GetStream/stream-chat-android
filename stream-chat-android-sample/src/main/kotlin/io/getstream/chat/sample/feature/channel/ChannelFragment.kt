package io.getstream.chat.sample.feature.channel

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.bindView
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.bindView
import io.getstream.chat.sample.R
import kotlinx.android.synthetic.main.fragment_channel.*

class ChannelFragment : Fragment(R.layout.fragment_channel) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cid = navArgs<ChannelFragmentArgs>().value.cid

        val messagesViewModel = MessageListViewModel(cid)
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

        ChannelHeaderViewModel(cid).bindView(channelHeaderView, this)
        MessageInputViewModel(cid).apply {
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

        val backButtonHandler = {
            messagesViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        channelHeaderView.onBackClick = { backButtonHandler() }

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
