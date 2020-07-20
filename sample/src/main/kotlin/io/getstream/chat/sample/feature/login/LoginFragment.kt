package io.getstream.chat.sample.feature.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.getstream.chat.sample.R
import io.getstream.chat.sample.data.user.User
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    private val adapter = UsersListAdapter {
        viewModel.userClicked(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usersList.adapter = adapter
        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is State.AvailableUsers -> renderAvailableUsers(it.availableUsers)
                    is State.LoggedIn -> redirectToChannelsScreen()
                }
            }
        )
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_channelsFragment)
    }

    private fun renderAvailableUsers(users: List<User>) {
        adapter.setUsers(users)
    }
}
