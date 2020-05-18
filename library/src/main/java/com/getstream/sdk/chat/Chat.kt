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
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.livedata.ChatDomain
import java.lang.IllegalStateException

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

    fun setUser(user: User, token: String, callbacks: InitConnectionListener)

    class Builder(private val context: Context) {
        var navigationHandler: ChatNavigationHandler? = null
        var style: ChatStyle = ChatStyle.Builder().build()
        var urlSigner: UrlSigner = DefaultUrlSigner()
        var markdown: ChatMarkdown = ChatMarkdownImpl(context)
        var chatDomain: ChatDomain? = null

        fun chatDomain(chatDomain: ChatDomain): Builder = apply {
            this.chatDomain = chatDomain
        }

        fun style(style: ChatStyle): Builder = apply {
            this.style = style
        }

        fun navigationHandler(navigationHandler: ChatNavigationHandler) = apply {
            this.navigationHandler = navigationHandler
        }

        fun build(): Chat {
            if (chatDomain == null) {
                throw IllegalStateException("ChatDomain must be initialized before calling Chat.Builder::build()")
            }
            return ChatImpl(ChatFontsImpl(style, context),
                    ChatStringsImpl(context),
                    navigationHandler,
                    urlSigner,
                    markdown
            ).apply {
                init()
                instance = this
            }
        }
    }

    companion object {
        private var instance: Chat? = null

        @JvmStatic
        fun getInstance(): Chat = instance?.let {
            return it
        } ?: throw IllegalStateException("Chat.Builder::build() must be called before obtaining Chat instance")
    }
}
