/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.ui.sample.feature.customlogin

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
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.common.trimmedText
import io.getstream.chat.ui.sample.databinding.FragmentCustomLoginBinding

class CustomLoginFragment : Fragment() {

    private val viewModel: CustomLoginViewModel by viewModels()

    private var _binding: FragmentCustomLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
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
            },
        )
    }

    private fun collectCredentials(): LoginCredentials {
        return LoginCredentials(
            apiKey = binding.apiKeyEditText.trimmedText,
            userId = binding.userIdEditText.trimmedText,
            userToken = binding.userTokenEditText.trimmedText,
            userName = binding.userNameEditText.trimmedText,
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
        findNavController().navigateSafely(R.id.action_customLoginFragment_to_homeFragment)
    }
}
