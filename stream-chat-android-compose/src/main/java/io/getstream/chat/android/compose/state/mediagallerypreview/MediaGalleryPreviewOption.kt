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

package io.getstream.chat.android.compose.state.mediagallerypreview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the information for media gallery preview options the user can take.
 *
 * @param title The title of the option in the list.
 * @param titleColor The color of the title option.
 * @param iconPainter The icon of the option.
 * @param iconColor The color of the icon.
 * @param action The action this option represents.
 * @param isEnabled If the action is currently enabled.
 */
public data class MediaGalleryPreviewOption(
    internal val title: String,
    internal val titleColor: Color,
    internal val iconPainter: Painter,
    internal val iconColor: Color,
    internal val action: MediaGalleryPreviewAction,
    internal val isEnabled: Boolean,
)
