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

package io.getstream.chat.android.compose.ui.attachments

import io.getstream.chat.android.compose.ui.attachments.factory.FileAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.GiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.ImageAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.LinkAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.QuotedAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.factory.UploadAttachmentFactory
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.GiphySizingMode

/**
 * Provides different attachment factories that build custom message content based on a given attachment.
 */
public object StreamAttachmentFactories {

    /**
     * The default max length of the link attachments description. We limit this, because for some links the description
     * can be too long.
     */
    private const val DEFAULT_LINK_DESCRIPTION_MAX_LINES = 5

    /**
     * Default attachment factories we provide, which can transform image, file and link attachments.
     *
     * @param linkDescriptionMaxLines - The limit of how long the link attachment descriptions can be.
     * @param giphyInfoType Used to modify the quality and dimensions of the rendered
     * Giphy attachments.
     * @param giphySizingMode Sets the Giphy container sizing strategy. Setting it to automatic
     * makes the container capable of adaptive resizing and ignore
     * [ChatTheme.dimens.attachmentsContentGiphyWidth] and [ChatTheme.dimens.attachmentsContentGiphyHeight]
     * dimensions, however you can still clip maximum dimensions using [ChatTheme.dimens.attachmentsContentGiphyMaxWidth]
     * and [ChatTheme.dimens.attachmentsContentGiphyMaxHeight].
     *
     * @return A [List] of various [AttachmentFactory] instances that provide different attachments support.
     */
    public fun defaultFactories(
        linkDescriptionMaxLines: Int = DEFAULT_LINK_DESCRIPTION_MAX_LINES,
        giphyInfoType: GiphyInfoType = GiphyInfoType.ORIGINAL,
        giphySizingMode: GiphySizingMode = GiphySizingMode.AUTOMATIC_RESIZING,
    ): List<AttachmentFactory> = listOf(
        UploadAttachmentFactory(),
        LinkAttachmentFactory(linkDescriptionMaxLines),
        GiphyAttachmentFactory(
            giphyInfoType = giphyInfoType,
            giphySizingMode = giphySizingMode
        ),
        ImageAttachmentFactory(),
        FileAttachmentFactory(),
    )

    /**
     * Default quoted attachment factories we provide, which can transform image, file and link attachments.
     *
     * @return a [List] of various [AttachmentFactory] instances that provide different quoted attachments support.
     */
    public fun defaultQuotedFactories(): List<AttachmentFactory> = listOf(
        QuotedAttachmentFactory()
    )
}
