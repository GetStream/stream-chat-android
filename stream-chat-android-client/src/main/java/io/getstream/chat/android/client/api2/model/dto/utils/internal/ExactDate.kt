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

package io.getstream.chat.android.client.api2.model.dto.utils.internal

import java.util.Date

/**
 * DTO to keep serialized date and also the original Date as Stirng as sent by backend.
 *
 * @param date The [Date] was parsed from backend of created locally.
 * @param rawDate The Date as a String. This is probably going to be the generated in the backend and can have
 * up to nanoseconds of precision.
 */
internal data class ExactDate(
    internal val date: Date,
    internal val rawDate: String,
)
