package io.getstream.chat.android.offline.errorhandler.internal

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class DeleteReactionErrorHandlerImplTest {

    @Test
    fun `when passing null as cid, no crash should happen`() {
        //We would like to check that no exceptions happens, so there's no need to assert anything.
        DeleteReactionErrorHandlerImpl(mock(), mock(), mock())
            .onDeleteReactionError(
                TestCall(Result(randomMessage())),
                null,
                randomString()
            )
    }
}
