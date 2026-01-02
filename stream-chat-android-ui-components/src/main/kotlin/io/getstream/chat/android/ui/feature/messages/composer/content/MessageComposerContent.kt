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
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView

/**
 * An interface that must be implemented by the content views of [MessageComposerView].
 */
public interface MessageComposerContent {
    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    public fun attachContext(messageComposerContext: MessageComposerContext)

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    public fun renderState(state: MessageComposerState)

    /**
     * Finds the first descendant view with the given [key].
     *
     * @param key the key to search for.
     */
    public fun findViewByKey(key: String): View? {
        return null
    }

    public companion object PredefinedKeys {
        public const val RECORD_AUDIO_BUTTON: String = "record_audio_button"
    }
}

public fun MessageComposerContent.asView(): View? = this as? View
