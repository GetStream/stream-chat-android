package io.getstream.chat.android.offline.repository.domain.poll.internal

import io.getstream.chat.android.offline.repository.domain.message.internal.PollDao
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class DatabasePollRepositoryTest {

    @Test
    fun testDeletePoll() = runTest {
        // given
        val dao = mock<PollDao>()
        val repository = DatabasePollRepository(dao)
        val pollId = randomString()
        // when
        repository.deletePoll(pollId)
        // then
        verify(dao).deletePoll(pollId)
    }
}
