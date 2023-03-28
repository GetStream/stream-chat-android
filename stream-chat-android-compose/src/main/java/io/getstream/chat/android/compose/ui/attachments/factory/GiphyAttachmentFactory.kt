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
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.GiphyAttachmentContent
import io.getstream.chat.android.compose.ui.theme.StreamDimens
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.GiphySizingMode

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
 *
 * @return Returns an instance of [AttachmentFactory] that is used to handle Giphys.
 */
@Suppress("FunctionName")
public fun GiphyAttachmentFactory(
    giphyInfoType: GiphyInfoType = GiphyInfoType.FIXED_HEIGHT_DOWNSAMPLED,
    giphySizingMode: GiphySizingMode = GiphySizingMode.ADAPTIVE,
    contentScale: ContentScale = ContentScale.Crop,
    onContentItemClick: (context: Context, Url: String) -> Unit = { context, url ->
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }
): AttachmentFactory =
    AttachmentFactory(
        canHandle = { attachments -> attachments.any { it.type == ModelType.attach_giphy } },
        content = @Composable { modifier, state ->
            GiphyAttachmentContent(
                modifier = modifier.wrapContentSize(),
                attachmentState = state,
                giphyInfoType = giphyInfoType,
                giphySizingMode = giphySizingMode,
                contentScale = contentScale,
                onItemClick = onContentItemClick,
            )
        },
    )
