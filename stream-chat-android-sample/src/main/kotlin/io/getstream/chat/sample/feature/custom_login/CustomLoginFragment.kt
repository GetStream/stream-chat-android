package io.getstream.chat.sample.feature.custom_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.initToolbar
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.common.showToast
import io.getstream.chat.sample.common.trimmedText
import io.getstream.chat.sample.databinding.FragmentCustomLoginBinding

class CustomLoginFragment : Fragment() {

    private val viewModel: CustomLoginViewModel by viewModels()

    private var _binding: FragmentCustomLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar(binding.toolbar)
        binding.sdkVersion.text =
            getString(R.string.sdk_version_template, STREAM_CHAT_VERSION)
        binding.loginButton.setOnClickListener {
            viewModel.loginButtonClicked(collectCredentials())
        }

        viewModel.state.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is State.RedirectToChannels -> redirectToChannelsScreen()
                    is State.Loading -> showLoading()
                    is State.Error -> showErrorMessage(it.errorMessage)
                    is State.ValidationError -> showValidationErrors(it.invalidFields)
                }
            }
        )
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
                ValidatedField.API_KEY -> binding.apiKeyInputLayout
                ValidatedField.USER_ID -> binding.userIdInputLayout
                ValidatedField.USER_TOKEN -> binding.userTokenInputLayout
            }.run {
                error = getString(R.string.custom_login_validation_error)
            }
        }
    }

    private fun clearValidationErrors() {
        binding.apiKeyInputLayout.error = null
        binding.userIdInputLayout.error = null
        binding.userTokenInputLayout.error = null
    }

    private fun redirectToChannelsScreen() {
        findNavController().navigateSafely(R.id.action_customLoginFragment_to_channelsFragment)
    }
}
