package io.getstream.chat.sample.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.common.showToast
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.databinding.FragmentUsersBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class UsersFragment : Fragment() {

    private val viewModel: UsersViewModel by viewModel()

    private val adapter = UsersListAdapter {
        viewModel.userClicked(this)
    }

    private var _binding: FragmentUsersBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.usersList.adapter = adapter
        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is State.AvailableUsers -> renderAvailableUsers(it.availableUsers)
                    is State.RedirectToChannels -> redirectToChannelsScreen()
                    is State.Loading -> changeLoadingIndicatorVisibility(true)
                    is State.Error -> showErrorMessage(it.errorMessage)
                }
            }
        )
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigateSafely(R.id.action_usersFragment_to_channelsFragment)
    }

    private fun renderAvailableUsers(users: List<User>) {
        changeLoadingIndicatorVisibility(false)
        adapter.setUsers(users)
    }

    private fun showErrorMessage(errorMessage: String?) {
        changeLoadingIndicatorVisibility(false)
        showToast(
            errorMessage ?: getString(R.string.backend_error_info)
        )
    }

    private fun changeLoadingIndicatorVisibility(isVisible: Boolean) {
        binding.loadingProgressBar.isVisible = isVisible
        binding.usersList.isVisible = !isVisible
    }
}
