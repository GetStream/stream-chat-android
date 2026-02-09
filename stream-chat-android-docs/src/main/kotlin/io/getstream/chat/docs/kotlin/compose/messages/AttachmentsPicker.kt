// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPicker
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/message-components/attachments-picker/#usage)
 */
private object AttachmentsPickerUsageSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // The rest of the UI

                        // The state if we need to show the picker or not
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker( // Add the picker to your UI
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                onAttachmentsSelected = {
                                    // Handle selected attachments
                                },
                                onDismiss = {
                                    // Handle dismiss
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Handling Actions](https://getstream.io/chat/docs/sdk/android/compose/message-components/attachments-picker/#handling-actions)
 */
private object AttachmentsPickerHandlingActionsSnippet {

    class MyActivity : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })
        val composerViewModel by viewModels<MessageComposerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // The rest of the UI

                        // The state if we need to show the picker or not
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker(
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                onAttachmentsSelected = { attachments ->
                                    // Dismiss the picker and store the attachments
                                    attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
                                    composerViewModel.addSelectedAttachments(attachments)
                                },
                                onDismiss = { // Dismiss the picker
                                    attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/message-components/attachments-picker/#customization)
 */
private object AttachmentsPickerCustomizationSnippet {

    class CustomPickerTabExample : AppCompatActivity() {
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "messaging:123",
            )
        }

        val attachmentsPickerViewModel by viewModels<AttachmentsPickerViewModel>(factoryProducer = { factory })

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker(
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                onAttachmentsSelected = { attachments ->
                                    // Handle selected attachments
                                },
                                onDismiss = {
                                    // Handle dismiss
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
