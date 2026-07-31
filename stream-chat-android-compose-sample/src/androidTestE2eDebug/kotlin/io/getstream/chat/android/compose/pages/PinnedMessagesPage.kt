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
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.compose.R as ComposeR
import io.getstream.chat.android.compose.sample.R as SampleR

/**
 * The pinned messages screen. Its rows carry no test tags, so a pinned message is matched by the
 * message text shown in the row preview.
 */
class PinnedMessagesPage {

    companion object {
        val title get() = By.text(appContext.getString(SampleR.string.pinned_messages_title))

        val emptyTitle
            get() = By.text(appContext.getString(ComposeR.string.stream_compose_pinned_message_list_empty_title))
    }
}
