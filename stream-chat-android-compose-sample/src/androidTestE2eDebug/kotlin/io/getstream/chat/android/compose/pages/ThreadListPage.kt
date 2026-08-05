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
import io.getstream.chat.android.e2e.test.uiautomator.resources
import io.getstream.chat.android.compose.R as ComposeR

/**
 * The threads list screen, opened from the threads tab of the sample bottom bar. Its rows carry no
 * test tags, so a thread is matched by the parent message preview and the reply count label.
 */
class ThreadListPage {

    companion object {
        val threadsTab = By.res("Stream_BottomBarThreadsTab")

        val parentMessagePreview = By.res("Stream_MessagePreview")

        val emptyTitle
            get() = By.text(appContext.getString(ComposeR.string.stream_compose_thread_list_empty_title))

        fun repliesCountLabel(count: Int) = By.text(
            resources.getQuantityString(ComposeR.plurals.stream_compose_thread_list_item_reply_count, count, count),
        )
    }
}
