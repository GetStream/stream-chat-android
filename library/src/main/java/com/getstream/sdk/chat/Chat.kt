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
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.InitConnectionListener

interface Chat {
    val navigator: ChatNavigator
    val strings: ChatStrings
    fun urlSigner(): UrlSigner
    val fonts: ChatFonts
    val onlineStatus: LiveData<OnlineStatus>
    val unreadMessages: LiveData<Number>
    val unreadChannels: LiveData<Number>
    val currentUser: LiveData<User>
    val markdown: ChatMarkdown
    val version: String

    fun setUser(
        user: User,
        token: String,
        callbacks: InitConnectionListener = object : InitConnectionListener() {}
    )

    fun disconnect()

    class Builder(private val apiKey: String, private val context: Context) {
        var navigationHandler: ChatNavigationHandler? = null
        var style: ChatStyle = ChatStyle.Builder().build()
        var urlSigner: UrlSigner = DefaultUrlSigner()
        var markdown: ChatMarkdown = ChatMarkdownImpl(context)
        var offlineEnabled: Boolean = false
        var notificationHandler: ChatNotificationHandler = ChatNotificationHandler(context)

        fun build(): Chat {
            return ChatImpl(
                ChatFontsImpl(style, context),
                ChatStringsImpl(context),
                navigationHandler,
                urlSigner,
                markdown,
                apiKey,
                context,
                offlineEnabled,
                notificationHandler
            ).apply {
                instance = this
            }
        }
    }

    companion object {
        private var instance: Chat? = null

        @JvmStatic
        fun getInstance(): Chat = instance
            ?: throw IllegalStateException("Chat.Builder::build() must be called before obtaining Chat instance")
    }
}
