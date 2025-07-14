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

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.onGiphyAttachmentContentClick
import io.getstream.chat.android.compose.ui.theme.StreamDimens
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.GiphySizingMode

/**
 * An [AttachmentFactory] that validates and shows Giphy attachments using [GiphyAttachmentContent].
 *
 * Has no "preview content", given that this attachment only exists after being sent.
 *
 * @param giphyInfoType Used to modify the quality and dimensions of the rendered
 * Giphy attachments.
 * @param giphySizingMode Sets the Giphy container sizing strategy. Setting it to automatic
 * makes the container capable of adaptive resizing and ignore
 * [StreamDimens.attachmentsContentGiphyWidth] and [StreamDimens.attachmentsContentGiphyHeight]
 * dimensions, however you can still clip maximum dimensions using [StreamDimens.attachmentsContentGiphyMaxWidth]
 * and [StreamDimens.attachmentsContentGiphyMaxHeight].
 * Setting it to fixed size mode will make it respect all given dimensions.
 * @param contentScale Used to determine the way Giphys are scaled inside the [Image] composable.
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param canHandle Lambda that checks if the factory can handle the given attachments.
 *
 * @return Returns an instance of [AttachmentFactory] that is used to handle Giphys.
 */
public class GiphyAttachmentFactory(
    giphyInfoType: GiphyInfoType = GiphyInfoType.FIXED_HEIGHT_DOWNSAMPLED,
    giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
    contentScale: ContentScale = ContentScale.Crop,
    onContentItemClick: (context: Context, url: String) -> Unit = ::onGiphyAttachmentContentClick,
    canHandle: (attachments: List<Attachment>) -> Boolean = { attachments -> attachments.any(Attachment::isGiphy) },
) : AttachmentFactory(
    type = Type.BuiltIn.GIPHY,
    canHandle = canHandle,
    content = @Composable { modifier, state ->
        GiphyAttachmentContent(
            modifier = modifier
                .wrapContentSize()
                .testTag("Stream_GiphyContent"),
            attachmentState = state,
            giphyInfoType = giphyInfoType,
            giphySizingMode = giphySizingMode,
            contentScale = contentScale,
            onItemClick = onContentItemClick,
        )
    },
)
