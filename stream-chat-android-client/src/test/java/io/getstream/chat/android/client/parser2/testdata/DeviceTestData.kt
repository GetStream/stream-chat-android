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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import org.intellij.lang.annotations.Language

internal object DeviceTestData {

    // `created_at` and `user_id` are required by the generated DeviceResponse; included in every
    // JSON below except where the test deliberately omits a different field.
    private const val CREATED_AT = "\"created_at\":\"2020-06-10T11:04:31.000Z\""
    private const val USER_ID = "\"user_id\":\"userId\""

    @Language("JSON")
    val jsonAllFields =
        """{"id":"token1","push_provider":"firebase","push_provider_name":"myProvider",$CREATED_AT,$USER_ID}"""

    @Language("JSON")
    val jsonOptionalFieldMissing =
        """{"id":"token1","push_provider":"firebase",$CREATED_AT,$USER_ID}"""

    @Language("JSON")
    val jsonMissingId =
        """{"push_provider":"firebase","push_provider_name":"myProvider",$CREATED_AT,$USER_ID}"""

    @Language("JSON")
    val jsonMissingPushProvider =
        """{"id":"token1","push_provider_name":"myProvider",$CREATED_AT,$USER_ID}"""

    val expectedDeviceAllFields = Device(
        token = "token1",
        pushProvider = PushProvider.FIREBASE,
        providerName = "myProvider",
    )

    val expectedDeviceOptionalMissing = Device(
        token = "token1",
        pushProvider = PushProvider.FIREBASE,
        providerName = null,
    )

    @Language("JSON")
    val jsonWithExplicitNulls =
        """{"id":"token1","push_provider":"firebase","push_provider_name":null,$CREATED_AT,$USER_ID}"""

    val expectedWithExplicitNulls = Device(
        token = "token1",
        pushProvider = PushProvider.FIREBASE,
        providerName = null,
    )
}
