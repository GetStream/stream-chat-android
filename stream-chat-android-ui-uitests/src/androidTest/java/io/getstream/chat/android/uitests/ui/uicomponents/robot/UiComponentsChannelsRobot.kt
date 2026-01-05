/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uitests.ui.uicomponents.robot

import io.getstream.chat.android.uitests.R

/**
 * A factory function for [ChannelsRobotUiComponents].
 */
internal fun channelsRobot(block: ChannelsRobotUiComponents.() -> Unit) = ChannelsRobotUiComponents().apply { block() }

/**
 * A robot that simulates user behavior on the channels screen.
 */
internal class ChannelsRobotUiComponents : BaseUiComponentsTestRobot() {

    /**
     * Clicks on any channel.
     */
    fun clickAnyChannel() {
        clickElementByIdWithDelay(R.id.channelItemView)
    }

    /**
     * Assert that any channel is displayed on the screen.
     */
    fun assertChannelIsDisplayed() {
        assertElementWithIdIsDisplayed(R.id.channelItemView)
    }
}
