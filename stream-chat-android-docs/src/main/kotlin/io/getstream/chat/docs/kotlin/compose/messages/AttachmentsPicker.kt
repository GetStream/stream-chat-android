// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.messages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CustomPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPicker
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

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
                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of the UI

                        // The state if we need to show the picker or not
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker( // Add the picker to your UI
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .height(350.dp),
                                onAttachmentsSelected = { attachments ->
                                    // Handle selected attachments
                                },
                                onTabClick = { _, _ -> },
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
                    Box(modifier = Modifier.fillMaxSize()) {

                        // The rest of the UI

                        // The state if we need to show the picker or not
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker(
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .height(350.dp),
                                onAttachmentsSelected = { attachments ->
                                    // Dismiss the picker and store the attachments
                                    attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
                                    composerViewModel.addSelectedAttachments(attachments)
                                },
                                onTabClick = { _, _ -> },
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

    class FullScreenPickerExample : AppCompatActivity() {
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
                    Box(modifier = Modifier.fillMaxSize()) {

                        // The state if we need to show the picker or not
                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker(
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                modifier = Modifier.fillMaxSize(), // Fill all the available space
                                shape = RectangleShape, // Use a shape without rounded corners
                                onAttachmentsSelected = { attachments ->
                                    // Handle selected attachments
                                },
                                onTabClick = { _, _ -> },
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
                    Box(modifier = Modifier.fillMaxSize()) {

                        val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments

                        if (isShowingAttachments) {
                            AttachmentPicker(
                                attachmentsPickerViewModel = attachmentsPickerViewModel,
                                modifier = Modifier.fillMaxSize(), // Fill all the available space
                                shape = RectangleShape, // Use a shape without rounded corners
                                onAttachmentsSelected = { attachments ->
                                    // Handle selected attachments
                                },
                                onTabClick = { _, _ -> },
                                onDismiss = {
                                    // Handle dismiss
                                }
                            )
                        }
                    }
                }
            }
        }

        class AttachmentsPickerCustomTabFactory : AttachmentsPickerTabFactory {

            override val attachmentsPickerMode: AttachmentsPickerMode
                get() = CustomPickerMode()

            override fun isPickerTabEnabled(channel: Channel): Boolean {
                // Return true if the tab should be enabled
                return true
            }

            @Composable
            override fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Custom tab",
                    tint = when {
                        isSelected -> ChatTheme.colors.primaryAccent
                        isEnabled -> ChatTheme.colors.textLowEmphasis
                        else -> ChatTheme.colors.disabled
                    },
                )
            }

            @Composable
            override fun PickerTabContent(
                onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
                attachments: List<AttachmentPickerItemState>,
                onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
                onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
                onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
            ) {

                LaunchedEffect(Unit) {
                    onAttachmentsChanged(emptyList())
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        style = ChatTheme.typography.title3Bold,
                        text = "Custom tab",
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                }
            }
        }
    }
}
