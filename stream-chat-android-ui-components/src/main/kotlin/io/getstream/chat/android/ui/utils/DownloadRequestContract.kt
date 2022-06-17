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

package io.getstream.chat.android.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import io.getstream.chat.android.client.call.Call

public class DownloadRequestContract : ActivityResultContract<() -> Call<Unit>, Pair<Boolean, () -> Call<Unit>>>() {
    private lateinit var downloadCall: () -> Call<Unit>
    override fun createIntent(context: Context, input: () -> Call<Unit>): Intent {
        downloadCall = input
        return ActivityResultContracts.RequestPermission()
            .createIntent(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, () -> Call<Unit>> {
        val result = ActivityResultContracts.RequestPermission().parseResult(resultCode, intent)
        return result to downloadCall
    }
}
