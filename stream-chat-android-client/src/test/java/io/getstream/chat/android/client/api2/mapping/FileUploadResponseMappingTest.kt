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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.models.UploadedFile
import io.getstream.result.Result
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class FileUploadResponseMappingTest {

    @Test
    fun `FileUploadResponse is correctly mapped to UploadedFile`() {
        val dto = Mother.randomFileUploadResponse()

        val result = dto.toUploadedFile()

        result shouldBeEqualTo Result.Success(
            UploadedFile(
                file = dto.file!!,
                thumbUrl = dto.thumbUrl,
            ),
        )
    }

    @Test
    fun `FileUploadResponse without a thumbnail is mapped with a null thumbUrl`() {
        val dto = Mother.randomFileUploadResponse(thumbUrl = null)

        val result = dto.toUploadedFile()

        result shouldBeEqualTo Result.Success(UploadedFile(file = dto.file!!, thumbUrl = null))
    }

    @Test
    fun `FileUploadResponse without a file URL fails instead of yielding a blank upload`() {
        val dto = Mother.randomFileUploadResponse(file = null)

        val result = dto.toUploadedFile()

        result shouldBeInstanceOf Result.Failure::class
    }
}
