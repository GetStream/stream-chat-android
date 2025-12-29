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

package io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments.factory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.attachments.factory.MediaAttachmentFactory
import io.getstream.chat.android.guides.catalog.compose.customizingimageandvideoattachments.ui.CustomPlayButton
import io.getstream.chat.android.models.AttachmentType

val customMediaAttachmentFactory = MediaAttachmentFactory(
    // Increase the maximum number of previewed items to 5
    maximumNumberOfPreviewedItems = 5,
    // Render a custom item above attachments inside the message list
    itemOverlayContent = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            CustomPlayButton(
                modifier = Modifier
                    .widthIn(10.dp)
                    .padding(2.dp)
                    .background(
                        color = Color(red = 255, blue = 255, green = 255, alpha = 220),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .fillMaxWidth(0.3f)
                    .aspectRatio(1.20f),
            )
        }
    },
    // Render a custom item above attachments inside the message composer
    previewItemOverlayContent = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            CustomPlayButton(
                modifier = Modifier
                    .padding(2.dp)
                    .background(
                        color = Color(red = 255, blue = 255, green = 255, alpha = 220),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .fillMaxWidth(0.35f)
                    .aspectRatio(1.20f),
            )
        }
    },
)
