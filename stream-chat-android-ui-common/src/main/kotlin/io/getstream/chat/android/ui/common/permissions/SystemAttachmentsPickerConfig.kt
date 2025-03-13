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

package io.getstream.chat.android.ui.common.permissions

/**
 * Defines the types of visual media that can be picked via the "System Attachment Picker".
 *
 * @param visualMediaAllowed If true, visual media items can be selected. Default is true.
 * @param visualMediaAllowMultiple If true, multiple visual media items can be selected. Default is false.
 * @param visualMediaType The types of visual media that can be picked. Default is [VisualMediaType.IMAGE_AND_VIDEO].
 * @param filesAllowed If true, files can be selected. Default is true.
 * @param captureImageAllowed If true, images can be captured. Default is true.
 * @param captureVideoAllowed If true, videos can be captured. Default is true.
 * @param pollAllowed If true, polls can be created. Default is true.
 */
public data class SystemAttachmentsPickerConfig(
    // Visual media attachments config
    public val visualMediaAllowed: Boolean = true,
    public val visualMediaAllowMultiple: Boolean = false,
    public val visualMediaType: VisualMediaType = VisualMediaType.IMAGE_AND_VIDEO,
    // Files attachments config
    public val filesAllowed: Boolean = true,
    // Capture media config
    public val captureImageAllowed: Boolean = true,
    public val captureVideoAllowed: Boolean = true,
    // Polls config
    public val pollAllowed: Boolean = true,
)
