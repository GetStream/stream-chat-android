package io.getstream.chat.docs.kotlin.ui.messages

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.feature.messages.ChannelActivity
import io.getstream.chat.android.ui.feature.messages.ChannelFragment
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.header.ChannelHeaderView
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.docs.R

/**
 * [Message List Screen](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/)
 */
class MessageListScreen {

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#usage)
     */
    fun usage() {

        fun startActivity(context: Context) {
            context.startActivity(ChannelActivity.createIntent(context, cid = "messaging:123"))
        }

        class MyChannelActivity : AppCompatActivity(R.layout.stream_ui_fragment_container) {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (savedInstanceState == null) {
                    val fragment = ChannelFragment.newInstance(cid = "messaging:123") {
                        showHeader(true)
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit()
                }
            }
        }
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#handling-actions)
     */
    fun handlingActions() {

        class MyChannelActivity : AppCompatActivity(R.layout.stream_ui_fragment_container), ChannelFragment.BackPressListener {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Add ChannelFragment to the layout
            }

            override fun onBackPress() {
                // Handle back press
            }
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/message-components/message-list-screen/#customization)
     */
    fun customization() {

        class CustomChannelFragment : ChannelFragment() {

            override fun setupChannelHeader(channelHeaderView: ChannelHeaderView) {
                super.setupChannelHeader(channelHeaderView)
                // Customize message list header view

                // For example, set a custom listener for the back button
                channelHeaderView.setBackButtonClickListener {
                    // Handle back press
                }
            }

            override fun setupMessageList(messageListView: MessageListView) {
                super.setupMessageList(messageListView)
                // Customize message list view
            }

            override fun setupMessageComposer(messageComposerView: MessageComposerView) {
                super.setupMessageComposer(messageComposerView)
                // Customize message composer view
            }
        }

        class CustomChannelActivity : ChannelActivity() {

            override fun createChannelFragment(cid: String, messageId: String?): ChannelFragment {
                return ChannelFragment.newInstance(cid) {
                    setFragment(CustomChannelFragment())
                    customTheme(R.style.StreamUiTheme)
                    showHeader(true)
                    messageId(messageId)
                }
            }
        }

        fun startActivity(context: Context) {
            context.startActivity(
                ChannelActivity.createIntent(
                    context = context,
                    cid = "messaging:123",
                    activityClass = CustomChannelActivity::class.java
                )
            )
        }
    }
}
