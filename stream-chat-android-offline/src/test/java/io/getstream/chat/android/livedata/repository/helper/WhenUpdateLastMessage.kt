package io.getstream.chat.android.livedata.repository.helper

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import io.getstream.chat.android.livedata.randomMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.VerifyNotCalled
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenUpdateLastMessage : BaseRepositoryHelperTest() {

    @Test
    fun `Given no channel in DB Should not do insert`() = runBlockingTest {
        When calling channels.select(eq("cid"), any(), any()) doReturn null

        sut.updateLastMessageForChannel("cid", randomMessage())

        VerifyNotCalled on channels that channels.insert(any())
    }
}
