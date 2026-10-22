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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIConversationDefaultIconsTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    @Test
    fun `every default icon`() {
        snapshot {
            val attachment = AIMessageAttachmentDefaults.icons()
            val actions = AIReplyActionsDefaults.icons()
            val conversation = AIConversationDefaults.icons()
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Group("Attachments")
                    Named("file", attachment.file)
                    Named("pdf", attachment.pdf)
                    Named("image", attachment.image)
                    Named("audio", attachment.audio)
                    Named("video", attachment.video)
                    Named("archive", attachment.archive)
                    Group("Conversation")
                    Named("scrollToLatest", conversation.scrollToLatest)
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Group("Reply actions")
                    Named("copy", actions.copy)
                    Named("copied", actions.copied)
                    Named("share", actions.share)
                    Named("regenerate", actions.regenerate)
                    Named("ratePositive", actions.ratePositive)
                    Named("ratedPositive", actions.ratedPositive)
                    Named("rateNegative", actions.rateNegative)
                    Named("ratedNegative", actions.ratedNegative)
                }
            }
        }
    }

    @Composable
    private fun Group(title: String) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }

    @Composable
    private fun Named(name: String, icon: Painter) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 12.dp).width(120.dp),
            )
        }
    }
}
