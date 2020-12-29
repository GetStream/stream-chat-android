package io.getstream.chat.ui.sample.feature.user_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_ID
import io.getstream.chat.ui.sample.application.EXTRA_CHANNEL_TYPE
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentUserLoginBinding

class UserLoginFragment : Fragment() {

    private var _binding: FragmentUserLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserLoginViewModel by viewModels()
    private val userLoginAdapter = UserLoginAdapter(
        userClickListener = { viewModel.onUiAction(UserLoginViewModel.UiAction.UserClicked(it)) },
        optionsClickListener = { viewModel.onUiAction(UserLoginViewModel.UiAction.ComponentBrowserClicked) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeStateAndEvents()
        viewModel.init(getNotificationChannelId())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupViews() {
        with(binding.usersList) {
            adapter = userLoginAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                ).apply {
                    setDrawable(requireContext().getDrawable(R.drawable.stream_ui_divider)!!)
                }
            )
        }
        with(binding.sdkVersion) {
            text = getString(R.string.sdk_version_template, STREAM_CHAT_VERSION)
            setOnClickListener {
                navigateSafely(R.id.action_userLoginFragment_to_componentBrowserHomeFragment)
            }
        }
    }

    private fun observeStateAndEvents() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is UserLoginViewModel.State.AvailableUsers -> {
                    binding.loginContainer.isVisible = true
                    userLoginAdapter.setUsers(it.availableUsers)
                }
            }
        }
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is UserLoginViewModel.UiEvent.RedirectToComponentBrowser -> {
                        navigateSafely(R.id.action_userLoginFragment_to_customLoginFragment)
                    }
                    is UserLoginViewModel.UiEvent.RedirectToChannels -> {
                        navigateSafely(R.id.action_userLoginFragment_to_homeFragment)
                    }
                    is UserLoginViewModel.UiEvent.RedirectToChannel -> {
                        navigateSafely(UserLoginFragmentDirections.actionUserLoginFragmentToChannelFragment(it.cid))
                    }
                    is UserLoginViewModel.UiEvent.Error -> {
                        showToast(it.errorMessage ?: getString(R.string.backend_error_info))
                    }
                }
            }
        )
    }

    private fun getNotificationChannelId(): String? {
        return activity?.intent?.let {
            val channelId = it.getStringExtra(EXTRA_CHANNEL_ID)
            val channelType = it.getStringExtra(EXTRA_CHANNEL_TYPE)
            if (!channelId.isNullOrBlank() && !channelType.isNullOrBlank()) {
                "$channelType:$channelId"
            } else {
                null
            }
        }
    }
}
