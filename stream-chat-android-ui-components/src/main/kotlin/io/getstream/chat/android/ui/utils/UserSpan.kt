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

package io.getstream.chat.android.ui.utils

import android.text.style.ClickableSpan
import android.view.View
import io.getstream.chat.android.models.User

/**
 * A [ClickableSpan] that represents a [User].
 *
 * This class is used to display a user's name in a [android.widget.TextView] and make it clickable.
 * @property user The user that this span represents.
 */
internal class UserSpan(val user: User) : ClickableSpan() {
    override fun onClick(widget: View) { /* no-op */ }
}
