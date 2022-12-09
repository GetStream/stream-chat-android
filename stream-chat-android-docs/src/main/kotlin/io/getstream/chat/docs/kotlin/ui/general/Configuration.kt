// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.general

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.markdown.MarkdownTextTransformer
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.android.ui.font.ChatFonts
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.ChannelNameFormatter
import io.getstream.chat.android.ui.helper.MessagePreviewFormatter
import io.getstream.chat.android.ui.helper.MimeTypeIconProvider
import io.getstream.chat.android.ui.helper.SupportedReactions
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.navigation.ChatNavigationHandler
import io.getstream.chat.android.ui.navigation.ChatNavigator
import io.getstream.chat.android.ui.navigation.destinations.ChatDestination
import io.getstream.chat.docs.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * [General Configuration](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/)
 */
private class ChatUiSnippets {

    private lateinit var context: Context

    /**
     * [Custom Reactions](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#custom-reactions)
     */
    fun customReactions() {
        // Create a drawable for the non-selected reaction option
        val loveDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!
        // Create a drawable for the selected reaction option and set a tint to it
        val loveDrawableSelected =
            ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!.apply { setTint(Color.RED) }

        // Create a map of reactions
        val supportedReactionsData = mapOf(
            "love" to SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected)
        )

        // Replace the default reactions with your custom reactions
        ChatUI.supportedReactions = SupportedReactions(context, supportedReactionsData)
    }

    /**
     * [Custom MIME Type Icons](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#custom-mime-type-icons)
     */
    fun customMimeTypeIcons() {
        ChatUI.mimeTypeIconProvider = MimeTypeIconProvider { mimeType ->
            when {
                // Generic icon for missing MIME type
                mimeType == null -> R.drawable.stream_ui_ic_file
                // Special icon for XLS files
                mimeType == "application/vnd.ms-excel" -> R.drawable.stream_ui_ic_file_xls
                // Generic icon for audio files
                mimeType.contains("audio") -> R.drawable.stream_ui_ic_file_mp3
                // Generic icon for video files
                mimeType.contains("video") -> R.drawable.stream_ui_ic_file_mov
                // Generic icon for other files
                else -> R.drawable.stream_ui_ic_file
            }
        }
    }

    /**
     * [Customizing Image Headers](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#adding-extra-headers-to-image-requests)
     */
    fun customizingImageHeaders() {
        ChatUI.imageHeadersProvider = object : ImageHeadersProvider {
            override fun getImageRequestHeaders(): Map<String, String> {
                return mapOf("token" to "12345")
            }
        }
    }

    /**
     * [Changing the Default Font](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#changing-the-default-font)
     */
    class ChangingTheDefaultFont : AppCompatActivity() {

        /**
         * Holds no significant value, it's just so that
         * we can simply use 'context' inside the tutorials
         * without specifying where it's coming from.
         */
        val context = applicationContext

        fun changingTheDefaultFont() {
            ChatUI.fonts = object : ChatFonts {

                // Fetch the font you want to use
                val font = ResourcesCompat.getFont(context, R.font.stream_roboto_regular)

                override fun setFont(textStyle: TextStyle, textView: TextView) {
                    textView.setTypeface(font, Typeface.BOLD)
                }

                override fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface) {
                    textView.setTypeface(font, Typeface.BOLD)
                }

                override fun getFont(textStyle: TextStyle): Typeface? = font
            }
        }
    }

    /**
     * [Transforming Message Text](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#transforming-message-text)
     */
    fun transformingMessageText() {
        ChatUI.messageTextTransformer =
            ChatMessageTextTransformer { textView: TextView, messageItem: MessageListItem.MessageItem ->
                // Transform messages to upper case.
                textView.text = messageItem.message.text.uppercase()
            }
    }

    /**
     * [Applying Markdown](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#markdown)
     */
    fun applyingMarkDown() {
        ChatUI.messageTextTransformer = MarkdownTextTransformer(context)
    }

    /**
     * [Customizing Navigator](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#navigator)
     */
    fun customizingNavigator() {
        val navigationHandler = ChatNavigationHandler { destination: ChatDestination ->
            // Perform a custom action here
            true
        }

        ChatUI.navigator = ChatNavigator(navigationHandler)
    }

    /**
     * [Customizing Channel Name Formatter](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#customizing-channelnameformatter)
     */
    fun customizingChannelNameFormatter() {
        ChatUI.channelNameFormatter = ChannelNameFormatter { channel, currentUser ->
            channel.name
        }
    }

    fun customizingMessagePreview() {
        ChatUI.messagePreviewFormatter = MessagePreviewFormatter { channel, message, currentUser ->
            message.text
        }
    }

    /**
     * [Customizing Date Formatter](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#customizing-dateformatter)
     */
    fun customizingDateFormatter() {
        ChatUI.dateFormatter = object : DateFormatter {
            private val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy")
            private val timeFormat: DateFormat = SimpleDateFormat("HH:mm")

            override fun formatDate(date: Date?): String {
                date ?: return ""
                return dateFormat.format(date)
            }

            override fun formatTime(date: Date?): String {
                date ?: return ""
                return timeFormat.format(date)
            }
        }
    }

    /**
     * [Customizing Attachments](https://getstream.io/chat/docs/sdk/android/v5/ui/general-customization/chatui/#customizing-attachments)
     */
    private class CustomizingAttachments {

        private fun customizeMessageList() {
            val attachmentFactoryManager = AttachmentFactoryManager(
                // Set your custom attachment factories here
            )

            ChatUI.attachmentFactoryManager = attachmentFactoryManager
        }

        private fun customizeMessageComposerOrInput() {
            val attachmentPreviewFactoryManager = AttachmentPreviewFactoryManager(
                // Set your custom attachment factories here
            )

            ChatUI.attachmentPreviewFactoryManager = attachmentPreviewFactoryManager
        }

        private fun customizeQuotedMessageContent() {
            val quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager(
                // Set your custom attachment factories here
            )

            ChatUI.quotedAttachmentFactoryManager = quotedAttachmentFactoryManager
        }
    }

    /**
     * [Disabling Video Thumbnails](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#disabling-video-thumbnails)
     */
    private fun disablingVideoThumbnails() {
        ChatUI.videoThumbnailsEnabled = false
    }
}
