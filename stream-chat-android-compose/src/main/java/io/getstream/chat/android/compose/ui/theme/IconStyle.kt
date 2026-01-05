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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/**
 * Represents the styling for the icon container component.
 *
 * @param size The size of the icon container.
 * @param padding The padding of the icon container.
 * @param icon The styling for the icon.
 */
public data class IconContainerStyle(
    val size: ComponentSize,
    val padding: ComponentPadding,
    val icon: IconStyle,
)

/**
 * Represents the styling for the icon component.
 *
 * @param painter The painter to use for the icon.
 * @param tint The tint color for the icon.
 * @param size The size of the icon.
 */
public data class IconStyle(
    val painter: Painter,
    val tint: Color,
    val size: ComponentSize,
)
