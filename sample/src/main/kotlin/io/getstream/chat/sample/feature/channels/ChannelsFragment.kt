package io.getstream.chat.sample.feature.channels

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.ChannelsViewModelImpl
import com.getstream.sdk.chat.viewmodel.bindView
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.common.visible
import io.getstream.chat.sample.data.user.UserRepository
import kotlinx.android.synthetic.main.fragment_channels.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ChannelsFragment : Fragment(R.layout.fragment_channels) {
    private val userRepo: UserRepository by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val loggedInUser = userRepo.user
        val user = User().apply {
            id = loggedInUser.id
            image = loggedInUser.image
            name = loggedInUser.name
        }

        Chat.getInstance().setUser(user, loggedInUser.token, object : InitConnectionListener() {
            override fun onSuccess(data: ConnectionData) {
                ChatLogger.instance.logD("ChannelsList", "User set successfully")
            }
            override fun onError(error: ChatError) {
                Timber.e(error)
            }
        })
        ChannelsViewModelImpl().apply {
            bindView(channelsList, this@ChannelsFragment)
            state.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is ChannelsViewModel.State.Loading -> {
                        loadingProgressbar.visible(it.isLoading)
                    }
                    is ChannelsViewModel.State.LoadingNextPage -> {
                        loadingNextPageProgressbar.visible(it.isLoading)
                    }
                }
            })
        }

        channelsList.setOnChannelClickListener {
            findNavController().navigate(ChannelsFragmentDirections.actionOpenChannel(it.cid))
        }

        addNewChannelButton.setOnClickListener {
            findNavController().navigate(R.id.action_to_create_channel)
        }
    }
}