/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.uiautomator

import android.graphics.Point
import android.graphics.Rect
import androidx.test.uiautomator.UiObject2

public fun Rect.bottomPoint(): Point {
    val x = right - ((right - left) / 2)
    val y = bottom
    return Point(x, y)
}

public fun Rect.leftPoint(): Point {
    val x = left
    val y = bottom - ((bottom - top) / 2)
    return Point(x, y)
}

public fun Long.toSeconds(): Int = (this / 1000).toInt()

public val Int.seconds: Long get() = (this * 1000).toLong()

public val UiObject2.height: Int get() = visibleBounds.height()

public val UiObject2.width: Int get() = visibleBounds.width()
