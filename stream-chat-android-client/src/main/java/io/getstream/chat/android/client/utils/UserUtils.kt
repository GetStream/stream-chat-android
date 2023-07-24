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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Partially merges [that] user data into [this] user data.
 */
@InternalStreamChatApi
public fun User.mergePartially(that: User): User = this.copy(
    role = that.role,
    createdAt = that.createdAt,
    updatedAt = that.updatedAt,
    lastActive = that.lastActive,
    banned = that.banned,
    name = that.name,
    image = that.image,
    extraData = that.extraData,
)
