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

package io.getstream.chat.android.compose.ui.scheduled

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.DefaultTimeZoneRule
import io.getstream.chat.android.compose.ui.PIXEL_4A_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import org.junit.Rule
import org.junit.Test

internal class ScheduledMessageListTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_4A_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @get:Rule
    val timeZoneRule = DefaultTimeZoneRule()

    @Test
    fun `scheduled message items`() {
        snapshotWithDarkMode { ScheduledMessageItems() }
    }

    @Test
    fun `scheduled message item statuses`() {
        snapshotWithDarkMode { ScheduledMessageItemStatuses() }
    }

    @Test
    fun `scheduled message item failures`() {
        snapshotWithDarkMode { ScheduledMessageItemFailures() }
    }

    @Test
    fun `scheduled message list loading`() {
        snapshotWithDarkMode { ScheduledMessageListLoading() }
    }

    @Test
    fun `scheduled message list empty`() {
        snapshotWithDarkMode { ScheduledMessageListEmpty() }
    }

    @Test
    fun `scheduled message list without search results`() {
        snapshotWithDarkMode { ScheduledMessageListEmptySearch() }
    }

    @Test
    fun `scheduled message list load failed`() {
        snapshotWithDarkMode { ScheduledMessageListLoadFailed() }
    }

    @Test
    fun `scheduled message actions`() {
        snapshotWithDarkMode { ScheduledMessageActions() }
    }

    @Test
    fun `scheduled message actions with custom actions`() {
        snapshotWithDarkMode { ScheduledMessageActionsWithCustomActions() }
    }

    @Test
    fun `scheduled message delete dialog`() {
        snapshotWithDarkMode {
            ScheduledMessageDeleteDialogContent(channelName = "Maya Ross", onConfirm = {}, onDismiss = {})
        }
    }

    @Test
    fun `scheduled message edit composer`() {
        snapshotWithDarkMode { ScheduledMessageEditComposerContent() }
    }

    @Test
    fun `scheduled message edit composer with a deleted quote`() {
        snapshotWithDarkMode { ScheduledMessageEditComposerQuoteDeleted() }
    }
}
