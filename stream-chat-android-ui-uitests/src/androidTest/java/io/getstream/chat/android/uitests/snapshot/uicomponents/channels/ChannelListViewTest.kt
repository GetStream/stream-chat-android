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

package io.getstream.chat.android.uitests.snapshot.uicomponents.channels

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.images.internal.StreamCoil
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListItem
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Before
import org.junit.Test

class ChannelListViewTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @OptIn(InternalStreamChatApi::class)
    @Before
    fun setup() {
        StreamCoil.setImageLoader { FakeImageLoader(context) }
    }

    @Test
    fun channelListWithContentState() {
        renderChannelListView {
            setChannels(
                listOf(
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel1().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member2(),
                            ),
                            messages = listOf(
                                TestData.message1(),
                            ),
                            channelLastMessageAt = TestData.date1(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel2().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member3(),
                            ),
                            messages = listOf(
                                TestData.message2(),
                            ),
                            channelLastMessageAt = TestData.date2(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel3().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member4(),
                            ),
                            messages = listOf(
                                TestData.message3(),
                            ),
                            channelLastMessageAt = TestData.date3(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel4().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member5(),
                            ),
                            messages = listOf(
                                TestData.message4(),
                            ),
                            channelLastMessageAt = TestData.date4(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.LoadingMoreItem,
                ),
            )
        }
    }

    @Test
    fun channelListWithEmptyState() {
        renderChannelListView {
            setChannels(emptyList())
        }
    }

    @Test
    fun channelListWithLoadingMoreState() {
        renderChannelListView {
            setChannels(
                listOf(
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel1().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member2(),
                            ),
                            messages = listOf(
                                TestData.message1(),
                            ),
                            channelLastMessageAt = TestData.date1(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel2().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member3(),
                            ),
                            messages = listOf(
                                TestData.message2(),
                            ),
                            channelLastMessageAt = TestData.date2(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel3().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member4(),
                            ),
                            messages = listOf(
                                TestData.message3(),
                            ),
                            channelLastMessageAt = TestData.date3(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.ChannelItem(
                        channel = TestData.channel4().copy(
                            members = listOf(
                                TestData.member1(),
                                TestData.member5(),
                            ),
                            messages = listOf(
                                TestData.message4(),
                            ),
                            channelLastMessageAt = TestData.date4(),
                        ),
                        typingUsers = emptyList(),
                        draftMessage = null,
                    ),
                    ChannelListItem.LoadingMoreItem,
                ),
            )
        }
    }

    @Test
    fun channelListWithLoadingState() {
        renderChannelListView {
            showLoadingView()
        }
    }

    private fun renderChannelListView(block: ChannelListView.() -> Unit) {
        val channelListView = ChannelListView(context)
        // .from(context)
        // .inflate(R.layout.view_channel_list, null, false) as ChannelListView

        channelListView.block()

        compareScreenshot(view = channelListView)
    }
}
