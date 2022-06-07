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

package io.getstream.chat.android.ui.message.input.internal

/**
 * Provides a method to validate input text in the message composer.
 */
internal object MessageTextValidator {

    /**
     * Checks if the message text can be sent to the server.
     *
     * @return If the given message text can be sent.
     */
    fun isMessageTextValid(text: String): Boolean {
        return text.isNotBlank() && !isEmptyGiphy(text)
    }

    /**
     * Checks if the message text contains an incomplete "giphy" command.
     *
     * @return If the given "giphy" message is valid, it contains the giphy command and non empty text.
     */
    private fun isEmptyGiphy(text: String): Boolean {
        val giphyCommand = "/giphy"

        if (text.startsWith(giphyCommand)) {
            val giphyContent = text.removePrefix(giphyCommand)

            return giphyContent.isBlank()
        }

        return false
    }
}
