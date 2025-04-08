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

package io.getstream.chat.android.client.extensions

import android.net.Uri
import androidx.core.net.toUri
import io.getstream.chat.android.client.streamcdn.StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_CROP_MODE
import io.getstream.chat.android.client.streamcdn.StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_HEIGHT
import io.getstream.chat.android.client.streamcdn.StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZED_WIDTH
import io.getstream.chat.android.client.streamcdn.StreamCdnResizeImageQueryParameterKeys.QUERY_PARAMETER_KEY_RESIZE_MODE
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StringExtensionsKtTest {

    @Test
    fun `given a String in snake case, it should be converted to lower cammel case`() {
        val text = "snake_case"
        val expected = "snakeCase"

        text.snakeToLowerCamelCase() `should be equal to` expected
    }

    @Test
    fun `given a String is lower cammel case, it should be parsed to getter`() {
        val text = "cammelCase"
        val expected = "getCammelCase"

        text.lowerCamelCaseToGetter() `should be equal to` expected
    }

    @Test
    fun `given empty cid, cidToTypeAndId throws exception`() {
        val cid = ""
        assertThrows<IllegalStateException> {
            cid.cidToTypeAndId()
        }
    }

    @Test
    fun `given invalid cid, cidToTypeAndId throws exception`() {
        val cid = "invalid"
        assertThrows<IllegalStateException> {
            cid.cidToTypeAndId()
        }
    }

    @Test
    fun `given a valid cid, cidToTypeAndId returns type and id`() {
        val cid = "messaging:123"
        val expected = "messaging" to "123"
        val result = cid.cidToTypeAndId()
        result shouldBeEqualTo expected
    }

    @Test
    fun `Given a Stream CDN link with original dimensions Should parse those dimensions correctly`() {
        val originalWidth = 2048
        val originalHeight = 1024

        val originalUrl = createStreamCdnImageLink(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
        )

        val dimensions = originalUrl.getStreamCdnHostedImageDimensions()

        dimensions?.originalWidth shouldBeEqualTo originalWidth
        dimensions?.originalHeight shouldBeEqualTo originalHeight
    }

    @Test
    fun `Given a Stream CDN image link not containing dimension parameters Should not append resizing parameters`() {
        val originalUrl = createStreamCdnImageLinkWithoutDimensionParameters()

        val resizeWidthPercentage = 0.5f
        val resizeHeightPercentage = 0.2f
        val resizeMode = StreamCdnResizeImageMode.CROP
        val cropMode = StreamCdnCropImageMode.CENTER

        val resizedUrl = originalUrl.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = resizeWidthPercentage,
            resizedHeightPercentage = resizeHeightPercentage,
            resizeMode = resizeMode,
            cropMode = cropMode,
        )

        val originalUri = originalUrl.toUri()
        val resizedUri = resizedUrl.toUri()

        val originalQueryParameterNames = originalUri.queryParameterNames
        val resizedQueryParameterNames = resizedUri.queryParameterNames

        originalQueryParameterNames shouldBeEqualTo resizedQueryParameterNames
    }

    @Test
    fun `Given a valid Stream CDN image link Should append resizing parameters`() {
        val originalUrl = createStreamCdnImageLink()

        val resizeWidthPercentage = 0.5f
        val resizeHeightPercentage = 0.2f
        val resizeMode = StreamCdnResizeImageMode.CROP
        val cropMode = StreamCdnCropImageMode.CENTER

        val resizedUrl = originalUrl.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = resizeWidthPercentage,
            resizedHeightPercentage = resizeHeightPercentage,
            resizeMode = resizeMode,
            cropMode = cropMode,
        )

        val resizedUri = resizedUrl.toUri()

        resizedUri.containsDuplicateQueryParameters() shouldBeEqualTo false

        val queryParameters = resizedUri.getMappedQueryParameters()

        val expectedResizeWidth = (queryParameters["ow"]!!.toInt() * resizeWidthPercentage).toInt()
        val expectedResizeHeight = (queryParameters["oh"]!!.toInt() * resizeHeightPercentage).toInt()

        queryParameters[QUERY_PARAMETER_KEY_RESIZED_WIDTH] shouldBeEqualTo expectedResizeWidth.toString()
        queryParameters[QUERY_PARAMETER_KEY_RESIZED_HEIGHT] shouldBeEqualTo expectedResizeHeight.toString()
        queryParameters[QUERY_PARAMETER_KEY_RESIZE_MODE] shouldBeEqualTo resizeMode.queryParameterName
        queryParameters[QUERY_PARAMETER_KEY_CROP_MODE] shouldBeEqualTo cropMode.queryParameterName
    }

    @Test
    fun `Given an already resized Stream CDN image link Should not append duplicate resizing parameters`() {
        val originalUrl = createStreamCdnImageLink()

        val resizeWidthPercentage = 0.5f
        val resizeHeightPercentage = 0.2f
        val resizeMode = StreamCdnResizeImageMode.CROP
        val cropMode = StreamCdnCropImageMode.CENTER

        val resizedUrl = originalUrl.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = resizeWidthPercentage,
            resizedHeightPercentage = resizeHeightPercentage,
            resizeMode = resizeMode,
            cropMode = cropMode,
        )

        val twiceResizedUrl = resizedUrl.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = resizeWidthPercentage,
            resizedHeightPercentage = resizeHeightPercentage,
            resizeMode = resizeMode,
            cropMode = cropMode,
        )

        val twiceResizedUri = twiceResizedUrl.toUri()

        twiceResizedUri.containsDuplicateQueryParameters() shouldBeEqualTo false
    }

    companion object {

        /**
         * Creates a Stream CDN link containing original width and height parameters.
         */
        fun createStreamCdnImageLink(
            originalWidth: Int = DEFAULT_ORIGINAL_WIDTH,
            originalHeight: Int = DEFAULT_ORIGINAL_HEIGHT,
        ) =
            "https://us-east.stream-io-cdn.com/1/images/IMAGE_NAME.jpg?Key-Pair-Id=SODHGWNRLG&Policy=akIjUneI9Kmbds2&Signature=dsnIjJ8-gfdgihih8-GkhdfgfdGFG32--KHJDFj349sfsdf~SFDf2~Fsdfgrg3~kjnooi23Jig-Kjoih34iW~k7Jbe2~Jnk33j-Fsiniiz2~Sfj23iJihn-Jinfnsiw2kS&oh=$originalHeight&ow=$originalWidth"

        /**
         * Creates a Stream CDN link without original width and height parameters.
         */
        fun createStreamCdnImageLinkWithoutDimensionParameters() =
            "https://us-east.stream-io-cdn.com/1/images/IMAGE_NAME.jpg?Key-Pair-Id=SODHGWNRLG&Policy=akIjUneI9Kmbds2&Signature=dsnIjJ8-gfdgihih8-GkhdfgfdGFG32--KHJDFj349sfsdf~SFDf2~Fsdfgrg3~kjnooi23Jig-Kjoih34iW~k7Jbe2~Jnk33j-Fsiniiz2~Sfj23iJihn-Jinfnsiw2kS"

        /**
         * Checks if the given link has any duplicate query parameters.
         */
        fun Uri.containsDuplicateQueryParameters(): Boolean {
            val queryParameterNames = this.queryParameterNames
            val distinctQueryParameterNames = queryParameterNames.distinct()

            return distinctQueryParameterNames.count() != queryParameterNames.count()
        }

        /**
         * A convenience extension function that creates a map of query parameter keys and values
         * so that they don't have to be individually fetched one by one.
         */
        fun Uri.getMappedQueryParameters(): Map<String, String> {
            val mutableMap = mutableMapOf<String, String>()

            this.queryParameterNames.forEach { queryParameter ->
                val queryParameterValue = this.getQueryParameter(queryParameter)

                if (queryParameterValue != null) {
                    mutableMap[queryParameter] = queryParameterValue
                }
            }

            return mutableMap
        }

        /**
         * Represents the default original width used to form a Stream CDN image link.
         */
        private const val DEFAULT_ORIGINAL_WIDTH: Int = 1024

        /**
         * Represents the default original height used to form a Stream CDN image link.
         */
        private const val DEFAULT_ORIGINAL_HEIGHT: Int = 512
    }
}
