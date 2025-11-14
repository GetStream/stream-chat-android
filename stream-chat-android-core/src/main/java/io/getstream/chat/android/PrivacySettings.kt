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

package io.getstream.chat.android

import androidx.compose.runtime.Immutable

/**
 * Represents the privacy settings of a user.
 *
 * @param typingIndicators Typing indicators settings.
 * @param deliveryReceipts Delivery receipts settings.
 * @param readReceipts Read receipts settings.
 */
@Immutable
public data class PrivacySettings(
    public val typingIndicators: TypingIndicators? = null,
    public val deliveryReceipts: DeliveryReceipts? = null,
    public val readReceipts: ReadReceipts? = null,
)

/**
 * Represents the typing indicators settings.
 * If false, the user typing events will not be sent to other users.
 *
 * @param enabled Whether typing indicators are enabled or not.
 */
@Immutable
public data class TypingIndicators(
    val enabled: Boolean = true,
)

/**
 * Represents the delivery receipts settings.
 * If false, the user delivery events will not be sent to other users, along with the user's delivery state.
 *
 * @param enabled Whether delivery receipts are enabled or not.
 */
@Immutable
public data class DeliveryReceipts(
    val enabled: Boolean = true,
)

/**
 * Represents the read receipts settings.
 * If false, the user read events will not be sent to other users, along with the user's read state.
 *
 * @param enabled Whether read receipts are enabled or not.
 */
@Immutable
public data class ReadReceipts(
    val enabled: Boolean = true,
)
