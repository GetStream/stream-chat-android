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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.utils.TimeProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.randomAttachment
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class AttachmentHelperTests {

    private lateinit var timeProvider: TimeProvider
    private lateinit var sut: AttachmentHelper

    @BeforeEach
    fun setup() {
        timeProvider = mock()
        sut = AttachmentHelper(timeProvider)
    }

    @Test
    fun `When has valid image url If attachment has null url Should return false`() {
        val attachment = randomAttachment(imageUrl = null)

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    /** [nonValidUrls] */
    @ParameterizedTest
    @MethodSource("nonValidUrls")
    fun `When has valid image url if attachment url is not valid Should return false`(notValidUrl: String) {
        val attachment = randomAttachment().copy(imageUrl = notValidUrl)

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    @Test
    fun `When has valid image url if attachment url is valid without Expires Should return true`() {
        val attachment = randomAttachment().copy(imageUrl = "https://www.someDomain.com/some-resource-id1.jpg")

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo true
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and failed to parse timestamp Should return false`() {
        val attachment =
            randomAttachment(imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=xxTTee")

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and timestamp is greater than current time Should return true`() {
        val currentTime = 1000L
        val timeStamp = currentTime + 100L
        whenever(timeProvider.provideCurrentTimeInSeconds()) doReturn currentTime
        val attachment =
            randomAttachment(imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=$timeStamp")

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo true
    }

    @Test
    fun `When has valid image url if attachment url is valid with Expires and timestamp is less than current time Should return false`() {
        val currentTime = 1000L
        val timeStamp = currentTime - 100L
        whenever(timeProvider.provideCurrentTimeInSeconds()) doReturn currentTime
        val attachment =
            randomAttachment(imageUrl = "https://www.someDomain.com/some-resource-id1.jpg?Expires=$timeStamp")

        val result = sut.hasValidImageUrl(attachment)

        result shouldBeEqualTo false
    }

    /** [streamUrlsInput] */
    @ParameterizedTest
    @MethodSource("streamUrlsInput")
    fun `Should detect stream link correctly`(inputAndResult: Pair<String, Boolean>) {
        val (inputUrl, expectedResult) = inputAndResult
        sut.hasStreamImageUrl(Attachment(imageUrl = inputUrl)) shouldBeEqualTo expectedResult
    }

    companion object {
        @JvmStatic
        fun nonValidUrls() = listOf(
            "someNotValidUrl",
            "https://????.com",
            "www.someDomainWithoutProtocol.com",
        )

        @JvmStatic
        @Suppress("MaxLineLength")
        fun streamUrlsInput() = listOf(
            "https://www.google.com/search?q=find+something" to false,
            "https://stream-chat-us-east-c4.imgix.net/102398/images/6f211fba-415b-4c67-a09f-fdd4dc0fd81c.Screenshot_2021-06-25-14-43-19-061_com.whatsa.jpg?ro=0&s=43257e963dbb993d65bd358d57b54a38" to true,
            "https://stream-chat-.imgix.net/102398/images/6f211fba-415b-4c67-a09f-fdd4dc0fd81c.Screenshot_2021-06-25-14-43-19-061_com.whatsa.jpg?ro=0&s=43257e963dbb993d65bd358d57b54a38" to false,
            "https://stream-chat&$^376473.io/failedLink" to false,
            "https://us-east.stream-io-cdn.com/link-to-some-file.png" to true,
            "https://stream-io-cdn.com/link-to-some-file.png" to false,
        )
    }
}
