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

package io.getstream.chat.android.client.errors

import io.getstream.result.Error

/**
 * Checks whether this error is the backend rejection of a message whose id already exists.
 *
 * The backend enforces message id uniqueness and rejects a repeated send of the same message with
 * a validation error. Receiving it means an earlier attempt already delivered the message (for
 * example, a send retried after its response was lost on a connection drop), so callers should
 * treat the send as successful instead of marking the message as failed.
 */
internal fun Error.isDuplicateMessageError(): Boolean =
    this is Error.NetworkError &&
        serverErrorCode == ChatErrorCode.VALIDATION_ERROR.code &&
        message.contains("already exists")
