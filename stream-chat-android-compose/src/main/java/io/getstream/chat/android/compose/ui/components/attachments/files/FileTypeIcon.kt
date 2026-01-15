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

package io.getstream.chat.android.compose.ui.components.attachments.files

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.ui.theme.StreamPrimitiveColors
import io.getstream.chat.android.compose.ui.util.FileIconData

@Composable
internal fun FileTypeIcon(data: FileIconData, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Image(
            painter = painterResource(data.resId),
            contentDescription = null,
            modifier = Modifier.wrapContentSize(),
        )
        if (data.typeName.isNotEmpty()) {
            // The file type name is treated as part of the icon and its size should not scale
            val fontSize = (IconLabelFontSize / LocalDensity.current.fontScale).sp

            Text(
                text = data.typeName,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = StreamPrimitiveColors.baseWhite,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clearAndSetSemantics {},
            )
        }
    }
}

private const val IconLabelFontSize = 8
