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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.ConnectionData

internal class TestInitCallback : Call.Callback<ConnectionData> {
    private var data: ConnectionData? = null
    private var error: ChatError? = null

    fun onSuccessIsCalled(): Boolean {
        return data != null
    }

    fun onErrorIsCalled(): Boolean {
        return error != null
    }

    override fun onResult(result: Result<ConnectionData>) {
        if (result.isSuccess) {
            data = result.data()
        } else {
            error = result.error()
        }
    }
}
