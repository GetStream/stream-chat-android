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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.socket.InitConnectionListener

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

    public fun setUser(
        user: User,
        token: String,
        callbacks: InitConnectionListener = object : InitConnectionListener() {}
    )

    public fun disconnect()

    public class Builder(private val apiKey: String, private val context: Context) {
        public var navigationHandler: ChatNavigationHandler? = null
        public var style: ChatStyle = ChatStyle.Builder().build()
        public var urlSigner: UrlSigner = DefaultUrlSigner()
        public var markdown: ChatMarkdown = ChatMarkdownImpl(context)
        public var offlineEnabled: Boolean = false
        public var notificationsConfig: NotificationConfig = NotificationConfig()

        public fun build(): Chat {
            return ChatImpl(
                ChatFontsImpl(style, context),
                ChatStringsImpl(context),
                navigationHandler,
                urlSigner,
                markdown,
                apiKey,
                context,
                offlineEnabled,
                notificationsConfig
            ).apply {
                instance = this
            }
        }
    }

    public companion object {
        private var instance: Chat? = null

        @JvmStatic
        public fun getInstance(): Chat = instance
            ?: throw IllegalStateException("Chat.Builder::build() must be called before obtaining Chat instance")
    }
}
