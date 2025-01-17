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

package io.getstream.chat.android.e2e.test.mockserver

public enum class AttachmentType(public val attachment: String) {
    IMAGE("image"),
    VIDEO("video"),
    FILE("file"),
}

public enum class ReactionType(public val reaction: String) {
    LOVE("love"),
    LOL("haha"),
    WOW("wow"),
    SAD("sad"),
    LIKE("like"),
}

public enum class MessageDeliveryStatus {
    READ,
    PENDING,
    SENT,
    FAILED,
    NIL, // is used when there should be no delivery status icon
}
