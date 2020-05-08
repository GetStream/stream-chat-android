package io.getstream.chat.sample.feature.channels

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.UserRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelsFragment : Fragment(R.layout.fragment_channels) {
    private val viewModel: ChannelsViewModel by viewModel()
    private val streamViewModel: ChannelListViewModel by viewModels()
    private val userRepo: UserRepository by inject()
    private val chat: Chat = Chat.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val client = chat.client
        val loggedInUser = userRepo.user
        val user = User().apply {
            id = loggedInUser.id
            image = loggedInUser.image
            name = loggedInUser.name
        }
        client.setUser(user, loggedInUser.token)

    }
}