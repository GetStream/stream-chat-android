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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Represents the styling for the text component.
 *
 * @param color The color of the component.
 * @param style The style of the component.
 * @param maxLines The maximum number of lines the component can have.
 * @param overflow The overflow behavior of the component.
 */
public data class TextComponentStyle(
    val color: Color,
    val style: TextStyle,
    val maxLines: Int = Int.MAX_VALUE,
    val overflow: TextOverflow = TextOverflow.Clip,
)

/**
 * Represents the styling for the text component.
 *
 * @param size The size of the component.
 * @param padding The padding of the component.
 * @param backgroundColor The background color of the component.
 * @param textStyle The text style of the component.
 */
public data class TextContainerStyle(
    val size: ComponentSize,
    val padding: ComponentPadding,
    val backgroundColor: Color,
    val textStyle: TextStyle,
)
