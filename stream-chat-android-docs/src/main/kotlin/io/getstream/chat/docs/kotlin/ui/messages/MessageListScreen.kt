package io.getstream.chat.docs.kotlin.ui.messages

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.feature.messages.MessageListActivity
import io.getstream.chat.android.ui.feature.messages.MessageListFragment
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.header.MessageListHeaderView
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
            context.startActivity(MessageListActivity.createIntent(context, cid = "messaging:123"))
        }

        class MyMessageListActivity : AppCompatActivity(R.layout.stream_ui_fragment_container) {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                if (savedInstanceState == null) {
                    val fragment = MessageListFragment.newInstance(cid = "messaging:123") {
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

        class MyMessageListActivity : AppCompatActivity(R.layout.stream_ui_fragment_container), MessageListFragment.BackPressListener {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Add MessageListFragment to the layout
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

        class CustomMessageListFragment : MessageListFragment() {

            override fun setupMessageListHeader(messageListHeaderView: MessageListHeaderView) {
                super.setupMessageListHeader(messageListHeaderView)
                // Customize message list header view

                // For example, set a custom listener for the back button
                messageListHeaderView.setBackButtonClickListener {
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

        class CustomMessageListActivity : MessageListActivity() {

            override fun createMessageListFragment(cid: String, messageId: String?): MessageListFragment {
                return MessageListFragment.newInstance(cid) {
                    setFragment(CustomMessageListFragment())
                    customTheme(R.style.StreamUiTheme)
                    showHeader(true)
                    messageId(messageId)
                }
            }
        }

        fun startActivity(context: Context) {
            context.startActivity(
                MessageListActivity.createIntent(
                    context = context,
                    cid = "messaging:123",
                    activityClass = CustomMessageListActivity::class.java
                )
            )
        }
    }
}
