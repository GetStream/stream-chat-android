package io.getstream.chat.ui.sample.feature.user_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_ID
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_TYPE
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.databinding.FragmentUserLoginBinding

class UserLoginFragment : Fragment() {

    private val viewModel: UserLoginViewModel by viewModels()

    private val adapter = UserLoginAdapter(
        userClickListener = { viewModel.userClicked(it) },
        optionsClickListener = ::redirectToCustomLoginScreen
    )

    private var _binding: FragmentUserLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.usersList.adapter = adapter
        binding.usersList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            ).apply {
                setDrawable(requireContext().getDrawable(R.drawable.stream_ui_divider)!!)
            }
        )
        binding.sdkVersion.text = getString(R.string.sdk_version_template, STREAM_CHAT_VERSION)

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is State.AvailableUsers -> renderAvailableUsers(it.availableUsers)
                is State.RedirectToChannels -> redirectToHomeScreen()
                is State.Loading -> changeLoadingIndicatorVisibility(true)
                is State.Error -> showErrorMessage(it.errorMessage)
            }
        }

        activity?.intent?.apply {
            val channelId = getStringExtra(EXTRA_CHANNEL_ID)
            val channelType = getStringExtra(EXTRA_CHANNEL_TYPE)
            if (!channelId.isNullOrBlank() && !channelType.isNullOrBlank()) {
                val cid = "$channelType:$channelId"
                viewModel.targetChannelDataReceived(cid)
            }
        }

        binding.sdkVersion.setOnLongClickListener {
            redirectToComponentBrowserScreen()
            true
        }
    }

    private fun redirectToHomeScreen() {
        findNavController().navigateSafely(R.id.action_userLoginFragment_to_homeFragment)
    }

    private fun redirectToCustomLoginScreen() {
        findNavController().navigateSafely(R.id.action_userLoginFragment_to_customLoginFragment)
    }

    private fun redirectToComponentBrowserScreen() {
        findNavController().navigateSafely(R.id.action_userLoginFragment_to_componentBrowserHomeFragment)
    }

    private fun renderAvailableUsers(users: List<SampleUser>) {
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
