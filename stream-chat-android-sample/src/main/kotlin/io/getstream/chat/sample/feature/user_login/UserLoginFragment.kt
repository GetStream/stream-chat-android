package io.getstream.chat.sample.feature.user_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.SamplePushNotificationRenderer
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.common.showToast
import io.getstream.chat.sample.data.user.SampleUser
import io.getstream.chat.sample.databinding.FragmentUserLoginBinding

class UserLoginFragment : Fragment() {

    private val viewModel: UserLoginViewModel by viewModels()

    private val adapter = UserLoginAdapter(
        userClickListener = { viewModel.userClicked(it) },
        optionsClickListener = ::redirectToCustomLoginScreen
    )

    private var _binding: FragmentUserLoginBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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
            )
        )
        binding.sdkVersion.text = getString(R.string.sdk_version_template, STREAM_CHAT_VERSION)

        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is State.AvailableUsers -> renderAvailableUsers(it.availableUsers)
                    is State.RedirectToChannels -> redirectToChannelsScreen()
                    is State.Loading -> changeLoadingIndicatorVisibility(true)
                    is State.Error -> showErrorMessage(it.errorMessage)
                    is State.RedirectToChannel -> redirectToChannel(it.cid)
                }
            }
        )

        requireActivity().intent?.apply {
            val channelId = getStringExtra(SamplePushNotificationRenderer.EXTRA_CHANNEL_ID)
            val channelType = getStringExtra(SamplePushNotificationRenderer.EXTRA_CHANNEL_TYPE)
            if (!channelId.isNullOrBlank() && !channelType.isNullOrBlank()) {
                val cid = "$channelType:$channelId"
                viewModel.targetChannelDataReceived(cid)
            }
        }

        requireActivity().apply {
            onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        activity?.finish()
                    }
                }
            )
        }
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigateSafely(R.id.action_userLoginFragment_to_channelsFragment)
    }

    private fun redirectToCustomLoginScreen() {
        findNavController().navigateSafely(R.id.action_userLoginFragment_to_customLoginFragment)
    }

    private fun redirectToChannel(cid: String) {
        findNavController().navigateSafely(
            UserLoginFragmentDirections.actionUserLoginFragmentToChannelFragment(cid)
        )
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
