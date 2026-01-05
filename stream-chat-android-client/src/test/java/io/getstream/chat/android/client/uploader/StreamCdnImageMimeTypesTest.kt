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

package io.getstream.chat.android.client.uploader

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class StreamCdnImageMimeTypesTest {

    @ParameterizedTest
    @MethodSource("mimeTypeArguments")
    fun `isImageMimeTypeSupported should return expected result`(mimeType: String?, expected: Boolean) {
        StreamCdnImageMimeTypes.isImageMimeTypeSupported(mimeType) shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        fun mimeTypeArguments() = listOf(
            Arguments.of("image/bmp", true),
            Arguments.of("image/gif", true),
            Arguments.of("image/jpeg", true),
            Arguments.of("image/png", true),
            Arguments.of("image/webp", true),
            Arguments.of("image/heic", true),
            Arguments.of("image/heic-sequence", true),
            Arguments.of("image/heif", true),
            Arguments.of("image/heif-sequence", true),
            Arguments.of("image/svg+xml", true),
            Arguments.of("image/unsupported", false),
            Arguments.of("text/plain", false),
            Arguments.of("application/json", false),
            Arguments.of(null, false),
        )
    }
}
