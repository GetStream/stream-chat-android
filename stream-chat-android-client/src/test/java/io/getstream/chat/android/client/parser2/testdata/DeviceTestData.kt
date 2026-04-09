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

    @Language("JSON")
    val jsonAllFields =
        """{"id":"token1","push_provider":"firebase","push_provider_name":"myProvider"}"""

    @Language("JSON")
    val jsonOptionalFieldMissing =
        """{"id":"token1","push_provider":"firebase"}"""

    @Language("JSON")
    val jsonMissingId =
        """{"push_provider":"firebase","push_provider_name":"myProvider"}"""

    @Language("JSON")
    val jsonMissingPushProvider =
        """{"id":"token1","push_provider_name":"myProvider"}"""

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
        """{"id":"token1","push_provider":"firebase","push_provider_name":null}"""

    val expectedWithExplicitNulls = Device(
        token = "token1",
        pushProvider = PushProvider.FIREBASE,
        providerName = null,
    )
}
