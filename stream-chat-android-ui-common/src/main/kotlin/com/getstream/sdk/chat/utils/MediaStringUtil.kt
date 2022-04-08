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

package com.getstream.sdk.chat.utils

import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

public object MediaStringUtil {

    @JvmStatic
    public fun convertVideoLength(videoLength: Long): String {
        val hours = videoLength / 3600
        val minutes = videoLength % 3600 / 60
        val seconds = videoLength % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @JvmStatic
    public fun convertFileSizeByteCount(bytes: Long): String {
        val unit = 1024
        if (bytes <= 0) return 0.toString() + " B"
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        val df = DecimalFormat("###.##")
        return df.format(bytes / unit.toDouble().pow(exp.toDouble())) + " " + pre + "B"
    }
}
