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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.view.View

public interface MessageComposerContentContainer : Iterable<MessageComposerContent?> {
    public val center: MessageComposerContent?
    public val centerOverlap: MessageComposerContent?
    public val leading: MessageComposerContent?
    public val trailing: MessageComposerContent?
    public val header: MessageComposerContent?
    public val footer: MessageComposerContent?

    public fun asView(): View
    public fun findViewByKey(key: String): View?
}

public fun MessageComposerContentContainer.findRecordAudioButton(): View? = findViewByKey(
    MessageComposerContent.RECORD_AUDIO_BUTTON,
)
