package io.getstream.chat.android.ui.message.composer

import android.text.Editable
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.internal.TextWatcherAdapter
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultLeadingContentBinding
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultTrailingContentBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public fun MessageComposerViewModel.bindView(view: MessageComposerView, lifecycleOwner: LifecycleOwner) {

    view.onSendMessageAction = {
        val text = input.value
        val attachments = selectedAttachments.value
        val message = buildNewMessage(text, attachments)
        sendMessage(message)
    }

    view.onInputTextChanged = { setMessageInput(it) }

    lifecycleOwner.lifecycleScope.launch {

        view.leadingContent = { container: ViewGroup ->
            val binding = StreamUiMessageComposerDefaultLeadingContentBinding.inflate(
                view.streamThemeInflater,
                container,
                false
            )
            binding.apply {
                attachmentsButton.isVisible = false
                commandsButton.isVisible = false
            }.root
        }

        view.centerContent = { container ->
            val binding = StreamUiMessageComposerDefaultCenterContentBinding.inflate(
                streamThemeInflater,
                container,
                false
            )
            binding.messageEditText.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    onInputTextChanged(s.toString())
                }
            })
            binding.clearCommandButton.isVisible = false
            binding.selectedAttachmentsContainer.isVisible = false
            binding.commandBadge.isVisible = false
            binding.messageReplyView.isVisible = false
            viewModelScope.launch {
                messageInputState.collect {
                    binding.messageEditText.apply {
                        val currentValue = text.toString()
                        val newValue = it.inputValue
                        if (newValue != currentValue) {
                            setText(it.inputValue)
                        }
                    }
                    binding.clearCommandButton.isVisible = it.inputValue.isNotEmpty()
                }
            }
            binding.root
        }

        view.trailingContent = { container ->
            val binding = StreamUiMessageComposerDefaultTrailingContentBinding.inflate(
                streamThemeInflater,
                container,
                false
            ).apply {
                viewModelScope.launch {
                    messageInputState.collect {
                        val sendButtonEnabled = it.inputValue.isNotEmpty()
                        sendMessageButtonDisabled.isVisible = !sendButtonEnabled
                        sendMessageButtonEnabled.isVisible = sendButtonEnabled
                    }
                }

                sendMessageButtonEnabled.setOnClickListener {
                    onSendMessageAction()
                }
            }
            binding.root
        }
    }
}
