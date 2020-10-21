package io.getstream.chat.sample.feature.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.BuildConfig
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.EXTRA_CHANNEL_ID
import io.getstream.chat.sample.common.navigateSafely
import io.getstream.chat.sample.common.showToast
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.loadingProgressBar
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sdkVersion.text = getString(R.string.login_sdk_version_template, BuildConfig.VERSION_NAME)
        developmentUsers.setOnClickListener {
            redirectToUsersScreen()
        }
        loginButton.setOnClickListener {
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

        activity?.intent?.getStringExtra(EXTRA_CHANNEL_ID)?.let { cid ->
            viewModel.targetChannelDataReceived(cid)
        }
    }

    private fun collectCredentials(): LoginCredentials {
        return LoginCredentials(
            apiKey = apiKeyEditText.text.toString().trim(),
            userId = userIdEditText.text.toString().trim(),
            userToken = userTokenEditText.text.toString().trim(),
            userName = userNameEditText.text.toString().trim()
        )
    }

    private fun showLoading() {
        changeLoadingIndicatorVisibility(true)
        clearValidationErrors()
    }

    private fun showErrorMessage(errorMessage: String?) {
        changeLoadingIndicatorVisibility(false)
        showToast(errorMessage ?: getString(R.string.backend_error_info))
    }

    private fun showValidationErrors(invalidFields: List<ValidatedField>) {
        changeLoadingIndicatorVisibility(false)

        invalidFields.forEach {
            when (it) {
                ValidatedField.API_KEY -> apiKeyEditText
                ValidatedField.USER_ID -> userIdEditText
                ValidatedField.USER_TOKEN -> userTokenEditText
            }.error = getString(R.string.login_validation_error)
        }
    }

    private fun clearValidationErrors() {
        apiKeyEditText.error = null
        userIdEditText.error = null
        userTokenEditText.error = null
    }

    private fun changeLoadingIndicatorVisibility(isVisible: Boolean) {
        loadingProgressBar.isVisible = isVisible
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
