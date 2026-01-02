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

package io.getstream.chat.android.compose.ui.theme.messages.attachments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ComponentSize
import io.getstream.chat.android.compose.ui.theme.IconStyle
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.ui.theme.StreamTypography

/**
 * Represents the theming for the file attachment component.
 *
 * @param background The background color for the file attachment.
 * @param itemShape The shape for the file attachment.
 * @param imageThumbnail The shape for the image thumbnail.
 * @param downloadIconStyle The styling for the download icon.
 * @param fileNameTextStyle The text style for the file name.
 * @param fileMetadataTextStyle The text style for the file metadata.
 */
@Immutable
public data class FileAttachmentTheme(
    public val background: Color,
    public val itemShape: Shape,
    public val imageThumbnail: Shape,
    public val downloadIconStyle: IconStyle,
    public val fileNameTextStyle: TextStyle,
    public val fileMetadataTextStyle: TextStyle,
) {

    public companion object {

        /**
         * Creates a default [FileAttachmentTheme].
         */
        @Composable
        public fun defaultTheme(
            typography: StreamTypography,
            shapes: StreamShapes,
            colors: StreamColors,
        ): FileAttachmentTheme = FileAttachmentTheme(
            background = colors.appBackground,
            itemShape = shapes.attachment,
            imageThumbnail = shapes.imageThumbnail,
            downloadIconStyle = IconStyle(
                painter = painterResource(id = R.drawable.stream_compose_ic_file_download),
                tint = colors.textHighEmphasis,
                size = ComponentSize.FillMaxSize,
            ),
            fileNameTextStyle = typography.bodyBold.copy(
                color = colors.textHighEmphasis,
            ),
            fileMetadataTextStyle = typography.footnote.copy(
                color = colors.textLowEmphasis,
            ),
        )
    }
}
