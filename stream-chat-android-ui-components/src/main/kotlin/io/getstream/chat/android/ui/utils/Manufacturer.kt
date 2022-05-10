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

package io.getstream.chat.android.ui.utils

import android.os.Build
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Checks whether the device manufacturer is Xiaomi ot not.
 * Fixes issue [#3255](https://github.com/GetStream/stream-chat-android/issues/3255)
 *
 * @return is manufacturer Xiaomi or not.
 */
@InternalStreamChatApi
internal fun isXiaomi(): Boolean {
    return Build.MANUFACTURER.lowercase() == "xiaomi"
}
