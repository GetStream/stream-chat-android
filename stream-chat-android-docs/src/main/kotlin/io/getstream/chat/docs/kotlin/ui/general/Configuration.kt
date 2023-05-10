// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.general

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.request.ImageRequest
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.images.ImageHeadersProvider
import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.markdown.MarkdownTextTransformer
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.MessagePreviewFormatter
import io.getstream.chat.android.ui.MimeTypeIconProvider
import io.getstream.chat.android.ui.SupportedReactions
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.avatar.AvatarStyle
import io.getstream.chat.android.ui.common.ChannelNameFormatter
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer
import io.getstream.chat.docs.R
import io.getstream.chat.docs.kotlin.ui.utility.GrayscaleTransformation
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * [General Configuration](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/)
 */
private class ChatUiSnippets {

    private lateinit var context: Context

    /**
     * [Custom Reactions](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#custom-reactions)
     */
    fun customReactions() {
        val loveDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!
        val loveDrawableSelected =
            ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!.apply { setTint(Color.RED) }
        val supportedReactionsData = mapOf(
            "love" to SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected)
        )
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

    fun defaultAvatar() {
        object : AvatarBitmapFactory(context) {
            override suspend fun createUserBitmap(user: User, style: AvatarStyle, avatarSize: Int): Bitmap? {
                return createDefaultUserBitmap(user, style, avatarSize)
            }
        }
    }

    /**
     * [Customizing Avatar](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#customizing-avatar)
     */
    fun customizingAvatar() {
        ChatUI.avatarBitmapFactory = object : AvatarBitmapFactory(context) {
            override suspend fun createUserBitmap(user: User, style: AvatarStyle, avatarSize: Int): Bitmap? {
                val imageResult = context.imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(user.image)
                        .apply {
                            if (!user.online) {
                                transformations(GrayscaleTransformation())
                            }
                        }
                        .build()
                )

                return (imageResult.drawable as? BitmapDrawable)?.bitmap
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
    fun changingTheDefaultFont() {
        ChatUI.fonts = object : ChatFonts {
            override fun setFont(textStyle: TextStyle, textView: TextView) {
                textStyle.apply(textView)
            }

            override fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface) {
                textStyle.apply(textView)
            }

            override fun getFont(textStyle: TextStyle): Typeface? = textStyle.font
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
            // Perform some custom action here
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
            private val dateFormatter = DateTimeFormatter.ofPattern("yy MM dd")
            private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            override fun formatDate(localDateTime: LocalDateTime?): String {
                localDateTime ?: return ""
                return dateFormatter.format(localDateTime)
            }

            override fun formatTime(localTime: LocalTime?): String {
                localTime ?: return ""
                return dateTimeFormatter.format(localTime)
            }

            override fun formatTime(localDateTime: LocalDateTime?): String {
                localDateTime ?: return ""
                return formatTime(localDateTime.toLocalTime())
            }
        }
    }
}
