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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.models.ConnectionData
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call

internal class TestInitCallback : Call.Callback<ConnectionData> {
    private var data: ConnectionData? = null
    private var error: Error? = null

    fun onSuccessIsCalled(): Boolean {
        return data != null
    }

    fun onErrorIsCalled(): Boolean {
        return error != null
    }

    override fun onResult(result: Result<ConnectionData>) {
        if (result.isSuccess) {
            data = result.getOrThrow()
        } else {
            error = result.errorOrNull()
        }
    }
}
