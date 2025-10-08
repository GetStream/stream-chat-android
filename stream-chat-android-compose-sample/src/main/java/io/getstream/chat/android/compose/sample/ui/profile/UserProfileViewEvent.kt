/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.profile

import io.getstream.result.Error

sealed interface UserProfileViewEvent {
    data object UpdateProfilePictureSuccess : UserProfileViewEvent
    sealed interface Failure : UserProfileViewEvent { val error: Error }
    data class LoadUnreadCountsError(override val error: Error) : Failure
    data class UpdateProfilePictureError(override val error: Error) : Failure
    data class RemoveProfilePictureError(override val error: Error) : Failure
    data class UpdatePushPreferencesError(override val error: Error) : Failure
}
