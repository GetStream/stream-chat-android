package io.getstream.chat.android.offline.utils

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.offline.repository.database.ChatDatabase

internal fun mockDb() = mock<ChatDatabase> {
    on { userDao() } doReturn mock()
    on { channelConfigDao() } doReturn mock()
    on { channelStateDao() } doReturn mock()
    on { queryChannelsDao() } doReturn mock()
    on { messageDao() } doReturn mock()
    on { reactionDao() } doReturn mock()
    on { syncStateDao() } doReturn mock()
    on { attachmentDao() } doReturn mock()
}

