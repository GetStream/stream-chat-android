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

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object StreamTokens {
    val borderStrokeSubtle = 1.2.dp

    val elevation3 = 4.dp

    val radius2xs = CornerSize(2.dp)
    val radiusXs = CornerSize(4.dp)
    val radiusSm = CornerSize(6.dp)
    val radiusMd = CornerSize(8.dp)
    val radiusLg = CornerSize(12.dp)
    val radiusXl = CornerSize(16.dp)
    val radius2xl = CornerSize(20.dp)
    val radius3xl = CornerSize(24.dp)
    val radius4xl = CornerSize(32.dp)
    val radiusFull = CornerSize(percent = 50)

    val size12 = 12.dp

    val spacing3xs = 2.dp
    val spacing2xs = 4.dp
    val spacingXs = 8.dp
    val spacingSm = 12.dp
    val spacingMd = 16.dp
    val spacingLg = 20.dp
    val spacingXl = 24.dp
    val spacing2xl = 32.dp
    val spacing3xl = 40.dp

    val fontWeightBold = FontWeight.Bold
    val fontWeightSemiBold = FontWeight.SemiBold
    val fontWeightRegular = FontWeight.Normal
    val fontSize2xs = 10.sp
    val fontSizeXs = 12.sp
    val fontSizeSm = 14.sp
    val fontSizeMd = 16.sp
    val fontSizeLg = 18.sp
    val fontSizeXl = 20.sp
    val lineHeightTightest = 10.sp
    val lineHeightTighter = 14.sp
    val lineHeightTight = 16.sp
    val lineHeightNormal = 20.sp
    val lineHeightRelaxed = 24.sp
}
