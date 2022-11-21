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

package io.getstream.chat.android.compose.state.imagepreview

import io.getstream.chat.android.models.Message

/**
 * Represents the actions the user can take with images in the Image Preview feature.
 *
 * @param message The message that the action is being performed on.
 */
internal sealed class ImagePreviewAction(internal val message: Message)

internal class Reply(message: Message) : ImagePreviewAction(message)

internal class ShowInChat(message: Message) : ImagePreviewAction(message)

internal class SaveImage(message: Message) : ImagePreviewAction(message)

internal class Delete(message: Message) : ImagePreviewAction(message)
