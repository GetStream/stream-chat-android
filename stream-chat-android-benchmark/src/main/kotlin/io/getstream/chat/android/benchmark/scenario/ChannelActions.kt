/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.benchmark.scenario

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import io.getstream.chat.android.benchmark.STANDARD_TIMEOUT
import io.getstream.chat.android.benchmark.flingElementDownUp
import io.getstream.chat.android.benchmark.waitAndFindObject

fun MacrobenchmarkScope.channelsExplore() = device.apply {
    channelsWaitForContent()
    channelsScrollDownUp()
}

fun MacrobenchmarkScope.channelsWaitForContent() = device.apply {
    wait(Until.hasObject(By.res("Stream_ChannelsScreen")), STANDARD_TIMEOUT)
}

fun MacrobenchmarkScope.channelsScrollDownUp() = device.apply {
    val channelList = waitAndFindObject(By.res("Stream_ChannelList"), STANDARD_TIMEOUT)
    flingElementDownUp(channelList)
}

fun MacrobenchmarkScope.navigateFromChannelsToMessages() = device.apply {
    waitAndFindObject(By.res("Stream_ChannelItem"), STANDARD_TIMEOUT).click()
    waitForIdle()
}
