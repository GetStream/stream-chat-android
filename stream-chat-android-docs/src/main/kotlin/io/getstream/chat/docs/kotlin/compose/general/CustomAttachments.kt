// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.general

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * [Custom Attachments](https://getstream.io/chat/docs/sdk/android/compose/general-customization/attachment-factory/#custom-attachments)
 */
private object CustomAttachmentsSnippet {

    class MyActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val myAttachmentFactories = listOf<AttachmentFactory>()
            val defaultFactories = StreamAttachmentFactories.defaultFactories()

            setContent {
                // override the default factories by adding your own
                ChatTheme(attachmentFactories = myAttachmentFactories + defaultFactories) {
                    // ChannelsScreen(...)
                }
            }
        }
    }
}
