/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.PollsConstants
import io.getstream.chat.android.ui.databinding.StreamUiFragmentCreatePollBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Represent the bottom sheet dialog that allows users to pick attachments.
 */
public class CreatePollDialogFragment : AppCompatDialogFragment() {

    private var _binding: StreamUiFragmentCreatePollBinding? = null
    private val binding get() = _binding!!
    private var createPollDialogListener: CreatePollDialogListener? = null
    private val createPollViewModel: CreatePollViewModel by viewModels()
    private val optionsAdapter: OptionsAdapter by lazy {
        OptionsAdapter { id, text -> createPollViewModel.onOptionTextChanged(id, text) }
    }
    private lateinit var sendMenuItem: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentCreatePollBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    private fun setCreatePollDialogListener(
        createPollDialogListener: CreatePollDialogListener,
    ): CreatePollDialogFragment {
        this.createPollDialogListener = createPollDialogListener
        return this
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        setupToolbar(binding.toolbar)
        binding.multipleAnswersSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.multipleAnswersCount.isVisible = isChecked
            createPollViewModel.setAllowMultipleVotes(isChecked)
            if (isChecked) {
                binding.multipleAnswersCount.requestFocus()
            }
        }
        binding.question.addTextChangedListener { editable ->
            createPollViewModel.onTitleChanged(editable.toString())
        }
        binding.anonymousPollSwitch.setOnCheckedChangeListener { _, isChecked ->
            createPollViewModel.setAnnonymousPoll(isChecked)
        }
        binding.suggestAnOptionSwitch.setOnCheckedChangeListener { _, isChecked ->
            createPollViewModel.setSuggestAnOption(isChecked)
        }
        createPollViewModel.options
            .onEach { binding.addOption.isEnabled = it.size < PollsConstants.MAX_NUMBER_OF_VOTES_PER_USER }
            .launchIn(lifecycleScope)
        binding.addOption.setOnClickListener {
            createPollViewModel.createOption()
        }
        binding.optionList.adapter = optionsAdapter
        binding.multipleAnswersCount.addTextChangedListener { editable ->
            createPollViewModel.setMaxAnswer(editable.toString().toIntOrNull())
        }
        lifecycleScope.launch {
            createPollViewModel.options.collectLatest { optionsAdapter.submitList(it) }
        }
        lifecycleScope.launch {
            createPollViewModel.pollIsReady.collectLatest {
                sendMenuItem.isEnabled = it
            }
        }
        lifecycleScope.launch {
            createPollViewModel.pollConfig.collectLatest { pollConfig ->
                pollConfig?.let {
                    createPollDialogListener?.onCreatePoll(it)
                    dismiss()
                }
            }
        }
        lifecycleScope.launch {
            createPollViewModel.multipleAnswersError.collectLatest { error ->
                binding.multipleAnswersCount.error = error?.let {
                    getString(
                        R.string.stream_ui_poll_error_multiple_answers,
                        PollsConstants.MIN_NUMBER_OF_MULTIPLE_ANSWERS.toString(),
                        PollsConstants.MAX_NUMBER_OF_VOTES_PER_USER.toString(),
                    )
                }
            }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener {
            createPollDialogListener?.onDismiss()
            dismiss()
        }
        ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_arrow_left)?.apply {
            setTint(ContextCompat.getColor(requireContext(), R.color.stream_ui_black))
        }?.let(toolbar::setNavigationIcon)
        toolbar.setTitle(getString(R.string.stream_ui_poll_create_a_poll_title))
        toolbar.inflateMenu(R.menu.stream_ui_create_poll_menu)
        sendMenuItem = toolbar.menu.findItem(R.id.action_create_poll)
        sendMenuItem.setOnMenuItemClickListener {
            createPollViewModel.createPollConfig()
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        createPollDialogListener = null
    }

    public companion object {
        public const val TAG: String = "create_poll_dialog_fragment"

        /**
         * Creates a new instance of [CreatePollDialogFragment].
         *
         * @return A new instance of [CreatePollDialogFragment].
         */
        public fun newInstance(createPollDialogListener: CreatePollDialogListener): CreatePollDialogFragment {
            return CreatePollDialogFragment()
                .setCreatePollDialogListener(createPollDialogListener)
        }
    }

    /**
     * The listener for the create poll dialog.
     */
    public interface CreatePollDialogListener {

        /**
         * Called when the user creates a poll.
         *
         * @param pollConfig The configuration of the poll.
         */
        public fun onCreatePoll(pollConfig: PollConfig)

        /**
         * Called when the dialog is dismissed without creating a poll.
         */
        public fun onDismiss()
    }
}
