package com.getstream.sdk.chat

import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.ChatNavigator
import com.getstream.sdk.chat.navigation.ChatNavigatorImpl
import com.getstream.sdk.chat.style.ChatFonts
import com.getstream.sdk.chat.style.ChatFontsImpl
import com.getstream.sdk.chat.style.ChatStyle
import com.getstream.sdk.chat.utils.strings.ChatStrings
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.uploader.FileUploader
import io.getstream.chat.android.livedata.ChatDomain

/**
 * ChatUX handles any configuration for the Chat UI elements. It replaces the older Chat class.
 */
public class ChatUX internal constructor(
    private val client: ChatClient,
    private val chatDomain: ChatDomain,
) {

    /** allows you to configure the fonts used by the UX components */
    public val fonts: ChatFonts

    /** global hook to overwrite any string lookups for the chat components */
    public val strings: ChatStrings

    /** navigation handler for customizing things such as the media browsing experience */
    public val navigationHandler: ChatNavigationHandler? = null

    /** interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules */
    public val markdown: ChatMarkdown

    /** url signing logic, enables you to add authorization tokens for images, video etc. */
    public val urlSigner: UrlSigner

    private val navigator: ChatNavigator

    init {
        if (navigationHandler != null) {
            navigator.setHandler(navigationHandler)
        }

    }

    val navigator: ChatNavigator = ChatNavigatorImpl()

    val version: String
        get() = BuildConfig.BUILD_TYPE + ":" + BuildConfig.VERSION_NAME


    protected fun init() {
        initLifecycle()
    }

    private fun initLifecycle() {
        StreamLifecycleObserver(object : LifecycleHandler {
            override fun resume() {
                client().reconnectSocket()
            }

            override fun stopped() {
                client().disconnectSocket()
            }
        })
    }

    public data class Builder(
        private var client: ChatClient,
        private var chatDomain: ChatDomain
    ) {

        val style = style: ChatStyle = ChatStyle.Builder().build()
        public var navigationHandler: ChatNavigationHandler? = null
        public var urlSigner: UrlSigner = UrlSigner.DefaultUrlSigner()
        public var markdown: ChatMarkdown = ChatMarkdownImpl(client.context)
        public var fonts = ChatFontsImpl(style, client.context)

        public fun withMarkdown(markdown: ChatMarkdown) {

        }

        public fun build(): ChatUX {

            instance = ChatUX(client, chatDomain)
            return instance
        }
    }

    public companion object {
        private lateinit var instance: ChatUX

        @JvmStatic
        public fun instance(): ChatUX {
            return instance
        }
    }

}