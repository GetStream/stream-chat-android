package io.getstream.chat.ui.sample.application

import android.app.Application
import android.os.Build
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactories
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolder
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.ui.sample.data.user.UserRepository

class App : Application() {

    // done for simplicity, a DI framework should be used in the real app
    val chatInitializer = ChatInitializer(this)
    val userRepository = UserRepository(this)

    override fun onCreate() {
        super.onCreate()
        chatInitializer.init(getApiKey())
        instance = this
        DebugMetricsHelper.init()
        Coil.setImageLoader(
            ImageLoader.Builder(this).componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(this@App))
                } else {
                    add(GifDecoder())
                }
            }.build()
        )
        ApplicationConfigurator.configureApp(this)
        initializeToggleService()

        ChatUI.attachmentFactories = AttachmentFactories(listOf(CustomAttachmentFactory()))
    }

    private fun getApiKey(): String {
        val user = userRepository.getUser()
        return if (user != SampleUser.None) {
            user.apiKey
        } else {
            AppConfig.apiKey
        }
    }

    @OptIn(InternalStreamChatApi::class)
    private fun initializeToggleService() {
        ToggleService.init(applicationContext, mapOf(ToggleService.TOGGLE_KEY_OFFLINE to BuildConfig.DEBUG))
    }

    companion object {
        lateinit var instance: App
            private set
    }
}

class CustomAttachmentFactory : AttachmentFactory {
    override fun canHandle(message: Message): Boolean {
        return message.attachments.isNotEmpty()
    }

    override fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): AttachmentViewHolder {
        return TextView(parent.context)
            .apply { setPadding(16) }
            .let { SampleAttachmentViewHolder(it, listeners) }
    }

    class SampleAttachmentViewHolder(
        private val textView: TextView,
        listeners: MessageListListenerContainer?,
    ) : AttachmentViewHolder(textView) {

        private lateinit var message: Message

        init {
            textView.setOnClickListener {
                listeners?.attachmentClickListener?.onAttachmentClick(message, message.attachments.first())
            }
            textView.setOnLongClickListener {
                listeners?.messageLongClickListener?.onMessageLongClick(message)
                true
            }
        }

        override fun onBindViewHolder(message: Message) {
            ChatLogger.get("ABC").logE("ABC bind")
            this.message = message

            textView.text = "Custom attachment " + message.id + " " + message.attachments.first().type
        }

        override fun onUnbindViewHolder() {
            ChatLogger.get("ABC").logE("ABC unbind")
        }

        override fun onViewAttachedToWindow() {
            ChatLogger.get("ABC").logE("ABC attach")
        }

        override fun onViewDetachedFromWindow() {
            ChatLogger.get("ABC").logE("ABC detach")
        }
    }
}
