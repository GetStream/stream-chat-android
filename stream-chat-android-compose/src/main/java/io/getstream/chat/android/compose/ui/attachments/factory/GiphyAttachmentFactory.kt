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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent
import io.getstream.chat.android.ui.utils.GiphyInfoType

/**
 * An [AttachmentFactory] that validates and shows Giphy attachments using [GiphyAttachmentContent].
 *
 * Has no "preview content", given that this attachment only exists after being sent.
 *
 * @param giphyInfoType Used to modify the quality and dimensions of the rendered
 * Giphy attachments.
 */
@Suppress("FunctionName")
public fun GiphyAttachmentFactory(
    giphyInfoType: GiphyInfoType = GiphyInfoType.FIXED_HEIGHT_DOWNSAMPLED,
    giphyScaling: GiphyScaling = GiphyScaling.ADAPTABLE
): AttachmentFactory =
    AttachmentFactory(
        canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
        content = @Composable { modifier, state ->
            GiphyAttachmentContent(
                modifier = modifier.wrapContentSize(),
                attachmentState = state,
                giphyInfoType = giphyInfoType,
                giphyScaling = giphyScaling
            )
        },
    )

public enum class GiphyScaling {
    ADAPTABLE,
    FILL_MAX_SIZE
}
