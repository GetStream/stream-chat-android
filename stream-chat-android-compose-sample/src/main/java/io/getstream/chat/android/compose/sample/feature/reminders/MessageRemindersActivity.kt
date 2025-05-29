package io.getstream.chat.android.compose.sample.feature.reminders

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message

/**
 * Activity displaying the list of message reminders.
 */
class MessageRemindersActivity : BaseConnectedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                MessageRemindersScreen(
                    modifier = Modifier.statusBarsPadding(),
                    onReminderClick = { reminder ->
                        reminder.message?.let(::openMessages)
                    },
                    onBack = ::finish,
                )
            }
        }
    }

    private fun openMessages(message: Message) {
        startActivity(
            MessagesActivity.createIntent(
                context = this,
                channelId = message.cid,
                messageId = message.id,
                parentMessageId = message.parentId,
            ),
        )
    }
}
