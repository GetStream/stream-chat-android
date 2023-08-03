/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.snapshot.compose.channels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.uitests.snapshot.compose.ComposeScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class SelectedChannelMenuTest : ComposeScreenshotTest() {

    @Test
    fun selectedChannelMenu() = runScreenshotTest {
        Box(modifier = Modifier.fillMaxSize()) {
            SelectedChannelMenu(
                modifier = Modifier.align(Alignment.BottomCenter),
                selectedChannel = TestData.channel1().apply {
                    members = listOf(
                        TestData.member1(),
                        TestData.member2(),
                        TestData.member3(),
                        TestData.member4(),
                        TestData.member5(),
                    )
                    messages = listOf(TestData.message1())
                },
                isMuted = false,
                currentUser = TestData.user1(),
                onChannelOptionClick = {},
                onDismiss = {},
            )
        }
    }
}
