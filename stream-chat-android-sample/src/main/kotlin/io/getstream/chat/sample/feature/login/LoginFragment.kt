package io.getstream.chat.sample.feature.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.BuildConfig
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.EXTRA_CHANNEL_ID
import io.getstream.chat.sample.application.EXTRA_CHANNEL_TYPE
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.common.showToast
import io.getstream.chat.sample.common.trimmedText
import io.getstream.chat.sample.databinding.FragmentLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModel()

    private var _binding: FragmentLoginBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.sdkVersion.text = getString(R.string.login_sdk_version_template, BuildConfig.STREAM_CHAT_UI_VERSION)
        binding.developmentUsers.setOnClickListener {
            redirectToUsersScreen()
        }
        binding.loginButton.setOnClickListener {
            viewModel.loginButtonClicked(collectCredentials())
        }

        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is State.RedirectToChannels -> redirectToChannelsScreen()
                    is State.RedirectToChannel -> redirectToChannel(it.cid)
                    is State.Loading -> showLoading()
                    is State.Error -> showErrorMessage(it.errorMessage)
                    is State.ValidationError -> showValidationErrors(it.invalidFields)
                }
            }
        )

        activity?.intent?.apply {
            val channelId = getStringExtra(EXTRA_CHANNEL_ID)
            val channelType = getStringExtra(EXTRA_CHANNEL_TYPE)
            if (!channelId.isNullOrBlank() && !channelType.isNullOrBlank()) {
                val cid = "$channelType:$channelId"
                viewModel.targetChannelDataReceived(cid)
            }
        }
    }

    private fun collectCredentials(): LoginCredentials {
        return LoginCredentials(
            apiKey = binding.apiKeyEditText.trimmedText,
            userId = binding.userIdEditText.trimmedText,
            userToken = binding.userTokenEditText.trimmedText,
            userName = binding.userNameEditText.trimmedText
        )
    }

    private fun showLoading() {
        binding.loadingProgressBar.isVisible = true
        clearValidationErrors()
    }

    private fun showErrorMessage(errorMessage: String?) {
        binding.loadingProgressBar.isVisible = false
        showToast(errorMessage ?: getString(R.string.backend_error_info))
    }

    private fun showValidationErrors(invalidFields: List<ValidatedField>) {
        binding.loadingProgressBar.isVisible = false
        invalidFields.forEach {
            when (it) {
                ValidatedField.API_KEY -> binding.apiKeyEditText
                ValidatedField.USER_ID -> binding.userIdEditText
                ValidatedField.USER_TOKEN -> binding.userTokenEditText
            }.error = getString(R.string.login_validation_error)
        }
    }

    private fun clearValidationErrors() {
        binding.apiKeyEditText.error = null
        binding.userIdEditText.error = null
        binding.userTokenEditText.error = null
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigateSafely(R.id.action_loginFragment_to_channelsFragment)
    }

    private fun redirectToUsersScreen() {
        findNavController().navigateSafely(R.id.action_loginFragment_to_usersFragment)
    }

    private fun redirectToChannel(cid: String) {
        findNavController().navigateSafely(
            LoginFragmentDirections.actionLoginFragmentToChannelFragment(cid)
        )
    }
}
