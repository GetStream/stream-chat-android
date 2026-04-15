// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.general

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * [Custom Attachments](https://getstream.io/chat/docs/sdk/android/compose/general-customization/attachment-factory/#custom-attachments)
 */
private object CustomAttachmentsSnippet {

    class CustomAttachments : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                // Override ChatComponentFactory to customise how attachments are rendered
                // in the message list and composer. Extend ChatComponentFactory and override
                // the relevant methods (e.g. CustomAttachmentContent, MessageComposerAttachments).
                ChatTheme(componentFactory = object : ChatComponentFactory {}) {
                    // Chat components
                }
            }
        }
    }
}
