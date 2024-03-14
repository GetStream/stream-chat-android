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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.positiveRandomLong
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageValidatorTest {

    private val messageValidator = MessageValidator(
        maxAttachmentSize = maxAttachmentSize,
        maxAttachmentCount = maxAttachmentCount,
        maxMessageLength = maxMessageLength,
    )

    /**
     * Test that [MessageValidator.validateMessage] returns the expected validation errors.
     *
     * @param canSendLinks if it is allowed to send links.
     * @param message The message to validate.
     * @param attachments The list of attachments to validate.
     * @param expectedErrors The expected validation errors.
     *
     * This method uses [provideValidationData] as a source for the test cases.
     */
    @ParameterizedTest
    @MethodSource("provideValidationData")
    fun `validateMessage returns expected validation errors`(
        canSendLinks: Boolean,
        message: String,
        attachments: List<Attachment>,
        expectedErrors: List<ValidationError>,
    ) {
        messageValidator.canSendLinks = canSendLinks

        val result = messageValidator.validateMessage(message, attachments)

        result `should be equal to` expectedErrors
    }
    companion object {

        private val maxAttachmentSize = 100 + positiveRandomLong(1000)
        private val maxAttachmentCount = 3 + positiveRandomInt(10)
        private val maxMessageLength = 100 + positiveRandomInt(1000)

        @JvmStatic
        @Suppress("LongMethod")
        fun provideValidationData() = listOf(
            Arguments.of(
                randomBoolean(),
                randomString(positiveRandomInt(maxMessageLength)),
                emptyList<Attachment>(),
                emptyList<ValidationError>(),
            ),
            (maxMessageLength + positiveRandomInt(1000)).let { messageLength ->
                Arguments.of(
                    randomBoolean(),
                    randomString(messageLength),
                    emptyList<Attachment>(),
                    listOf(ValidationError.MessageLengthExceeded(messageLength, maxMessageLength)),
                )
            },
            Arguments.of(
                randomBoolean(),
                randomString(positiveRandomInt(maxMessageLength)),
                List(positiveRandomInt(maxAttachmentCount)) {
                    randomAttachment(
                        fileSize = positiveRandomLong(maxAttachmentSize).toInt(),
                    )
                },
                emptyList<ValidationError>(),
            ),
            (maxAttachmentCount + positiveRandomInt(10)).let { attachmentCount ->
                Arguments.of(
                    randomBoolean(),
                    randomString(positiveRandomInt(maxMessageLength)),
                    List(attachmentCount) {
                        randomAttachment(
                            fileSize = positiveRandomLong(maxAttachmentSize).toInt(),
                        )
                    },
                    listOf(ValidationError.AttachmentCountExceeded(attachmentCount, maxAttachmentCount)),
                )
            },
            randomAttachment(
                fileSize = (maxAttachmentSize + positiveRandomLong(100)).toInt(),
            ).let { invalidAttachment ->
                Arguments.of(
                    randomBoolean(),
                    randomString(positiveRandomInt(maxMessageLength)),
                    (
                        List(positiveRandomInt(maxAttachmentCount - 1)) {
                            randomAttachment(
                                fileSize = positiveRandomLong(maxAttachmentSize).toInt(),
                            )
                        } + invalidAttachment
                        ).shuffled(),
                    listOf(
                        ValidationError.AttachmentSizeExceeded(
                            listOf(invalidAttachment),
                            maxAttachmentSize,
                        ),
                    ),
                )
            },
            Arguments.of(
                false,
                randomString() + " https://getstream.io " + randomString(),
                emptyList<Attachment>(),
                listOf(ValidationError.ContainsLinksWhenNotAllowed),
            ),
        )
    }
}
