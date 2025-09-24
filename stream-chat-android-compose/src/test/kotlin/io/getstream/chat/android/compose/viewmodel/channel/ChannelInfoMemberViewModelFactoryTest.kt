/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.viewmodel.channel

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf

internal class ChannelInfoMemberViewModelFactoryTest {

    @Test
    fun `create should return correct instance`() {
        val sut = Fixture().get()

        val viewModel = sut.create(ChannelInfoMemberViewModel::class.java)

        assertInstanceOf<ChannelInfoMemberViewModel>(viewModel)
    }

    @Test
    fun `create should throw IllegalArgumentException for unsupported ViewModel class`() {
        val sut = Fixture().get()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            sut.create(ViewModel::class.java)
        }

        assertEquals(
            "ChannelInfoMemberViewModelFactory can only create instances of ChannelInfoMemberViewModel",
            exception.message,
        )
    }

    private class Fixture {
        fun get() = ChannelInfoMemberViewModelFactory(
            cid = randomCID(),
            memberId = randomString(),
        )
    }
}
