package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User

internal class ChatDomainFactory {
    fun create(
        context: Context,
        chatClient: ChatClient,
        user: User,
        database: ChatDatabase?,
        offlineEnabled: Boolean,
        userPresence: Boolean,
        recoveryEnabled: Boolean
    ): ChatDomainImpl {
        return ChatDomainImpl(
            chatClient,
            user,
            database ?: createDatabase(context, user, offlineEnabled),
            createHandler(),
            offlineEnabled,
            userPresence,
            recoveryEnabled
        )
    }

    private fun createDatabase(context: Context, user: User, offlineEnabled: Boolean) = if (offlineEnabled) {
        ChatDatabase.getDatabase(context, user.id)
    } else {
        Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build()
    }

    private fun createHandler() = Handler(Looper.getMainLooper())
}
