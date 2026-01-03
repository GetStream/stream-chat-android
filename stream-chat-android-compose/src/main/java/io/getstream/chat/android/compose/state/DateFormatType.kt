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

package io.getstream.chat.android.compose.state

/**
 * Represents the types of formatting we provide with our formatter API.
 *
 * This is used to control if we want to show timestamps, or full date information.
 */
public enum class DateFormatType {
    /**
     * Represents a date format with only timestamps.
     */
    TIME,

    /**
     * Represents a date format with full date information.
     */
    DATE,

    /**
     * Represents a date format with relative time information.
     */
    RELATIVE,
}
