package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.offline.internal.repository.database.ChatDatabase
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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
