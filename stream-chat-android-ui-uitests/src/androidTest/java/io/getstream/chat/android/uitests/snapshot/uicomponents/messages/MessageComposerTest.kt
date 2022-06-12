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

package io.getstream.chat.android.uitests.snapshot.uicomponents.messages

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.common.state.ValidationError
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.message.composer.MessageComposerView
import org.junit.Test

@OptIn(InternalStreamChatApi::class)
class MessageComposerTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * GIVEN Message composer with no input
     * WHEN Displaying the composer
     * THEN Should show enabled attachments button
     * AND Should show enabled commands button
     * AND Should show empty enabled input
     * AND Should show disabled send button
     */
    @Test
    fun messageComposerWithEmptyInput() {
        renderMessageComposer(
            MessageComposerState(
                ownCapabilities = ChannelCapabilities.toSet()
            )
        )
    }

    /**
     * GIVEN Message composer with text input
     * WHEN Displaying the composer
     * THEN Should show enabled attachments button
     * AND Should show disabled commands button
     * AND Should show text input in the composer
     * AND Should show enabled send button
     */
    @Test
    fun messageComposerWithTextInput() {
        renderMessageComposer(
            MessageComposerState(
                inputValue = "Message text",
                ownCapabilities = ChannelCapabilities.toSet()
            )
        )
    }

    /**
     * GIVEN Message composer without "send-message" capability
     * WHEN Displaying the composer
     * THEN Should not show attachments button
     * AND Should not show commands button
     * AND Should show disabled input with corresponding hint
     * AND Should show disabled send button
     */
    @Test
    fun messageComposerWithoutSendMessageCapability() {
        renderMessageComposer(
            MessageComposerState(
                ownCapabilities = ChannelCapabilities.toSet() - ChannelCapabilities.SEND_MESSAGE
            )
        )
    }

    /**
     * GIVEN Message composer without "upload-file" capability
     * AND Long message input
     * WHEN Displaying the composer
     * THEN Should not show attachments button
     * AND Should show commands button
     * AND Should show empty enabled input
     * AND Should show disabled send button
     */
    @Test
    fun messageComposerWithoutUploadFileCapability() {
        renderMessageComposer(
            MessageComposerState(
                ownCapabilities = ChannelCapabilities.toSet() - ChannelCapabilities.UPLOAD_FILE
            )
        )
    }

    /**
     * GIVEN Message composer with "message length" validation error
     * THEN Should show attachments button
     * AND Should show commands button
     * AND Should show the input with long message
     * AND Should show disabled send button
     */
    @Test
    fun messageComposerWithMessageLengthValidationError() {
        renderMessageComposer(
            MessageComposerState(
                inputValue = "Message text".repeat(100),
                validationErrors = listOf(ValidationError.MessageLengthExceeded(1200, 1000)),
                ownCapabilities = ChannelCapabilities.toSet()
            )
        )
    }

    private fun renderMessageComposer(state: MessageComposerState) {
        val messageComposerView = MessageComposerView(context)
        val containerView = FrameLayout(context)
        containerView.addView(
            messageComposerView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        messageComposerView.renderState(state)

        compareScreenshot(containerView)
    }
}
