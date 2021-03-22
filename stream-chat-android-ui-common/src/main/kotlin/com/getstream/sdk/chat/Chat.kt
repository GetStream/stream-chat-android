package com.getstream.sdk.chat

import android.content.Context
import androidx.lifecycle.LiveData
import com.getstream.sdk.chat.UrlSigner.DefaultUrlSigner
import com.getstream.sdk.chat.enums.OnlineStatus
import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.ChatNavigator
import com.getstream.sdk.chat.style.ChatFonts
import com.getstream.sdk.chat.style.ChatFontsImpl
import com.getstream.sdk.chat.style.ChatStyle
import com.getstream.sdk.chat.utils.strings.ChatStrings
import com.getstream.sdk.chat.utils.strings.ChatStringsImpl
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.uploader.FileUploader

@Deprecated(
    message = "The Chat interface has been replaced by ChatUI. Have a look at the updated Android tutorial for details.",
    level = DeprecationLevel.ERROR,
)
public interface Chat {
    public val navigator: ChatNavigator
    public val strings: ChatStrings
    public fun urlSigner(): UrlSigner
    public val fonts: ChatFonts
    public val onlineStatus: LiveData<OnlineStatus>
    public val unreadMessages: LiveData<Number>
    public val unreadChannels: LiveData<Number>
    public val currentUser: LiveData<User>
    public val markdown: ChatMarkdown
    public val version: String

    /**
     * Sets the user and connects to Stream's API
     *
     * @param user The user object
     * @param token The token, typically provided by your backend during authentication
     * @param callbacks optional callback to listen to connect events
     *
     * @see User
     */
    public fun setUser(
        user: User,
        token: String,
        callbacks: InitConnectionListener = object : InitConnectionListener() {},
    )

    public fun disconnect()

    public class Builder(private val apiKey: String, private val context: Context) {
        public var navigationHandler: ChatNavigationHandler? = null
        public var style: ChatStyle = ChatStyle.Builder().build()
        public var urlSigner: UrlSigner = DefaultUrlSigner()
        public var markdown: ChatMarkdown = ChatMarkdownImpl(context)
        public var offlineEnabled: Boolean = false
        public var notificationHandler: ChatNotificationHandler = ChatNotificationHandler(context)
        public var chatLogLevel: ChatLogLevel = ChatLogLevel.NOTHING
        public var chatLoggerHandler: ChatLoggerHandler? = null
        public var fileUploader: FileUploader? = null

        @Suppress("DEPRECATION_ERROR")
        public fun build(): Chat = ChatImpl(
            ChatFontsImpl(style, context),
            ChatStringsImpl(context),
            navigationHandler,
            urlSigner,
            markdown,
            apiKey,
            context,
            offlineEnabled,
            notificationHandler,
            chatLogLevel,
            chatLoggerHandler,
            fileUploader
        ).apply {
            instance = this
        }
    }

    @Suppress("DEPRECATION_ERROR")
    public companion object {
        private var instance: Chat? = null

        @JvmStatic
        @Deprecated(
            message = "Use Chat.instance() instead",
            replaceWith = ReplaceWith("Chat.instance()"),
            level = DeprecationLevel.ERROR,
        )
        public fun getInstance(): Chat = instance
            ?: throw IllegalStateException("Chat.Builder::build() must be called before obtaining Chat instance")

        @JvmStatic
        public fun instance(): Chat = instance
            ?: throw IllegalStateException("Chat.Builder::build() must be called before obtaining Chat instance")
    }
}
