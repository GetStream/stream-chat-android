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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By

class ThreadPage : MessageListPage() {

    class ThreadList {

        companion object {
            val repliesCountLabel = By.res("Stream_RepliesCount")
            val alsoSentToChannelLabel = By.text("Also sent to the channel")
            val alsoSendToChannelCheckbox = By.res("Stream_AlsoSendToChannel")
        }
    }
}
