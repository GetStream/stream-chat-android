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

package io.getstream.chat.android.ui.common.contract.internal

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class SelectFilesContract : ActivityResultContract<Boolean, List<Uri>>() {

    override fun createIntent(
        context: Context,
        input: Boolean,
    ): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        addCategory(Intent.CATEGORY_OPENABLE)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        return intent?.data.takeIf { resultCode == Activity.RESULT_OK }?.let { listOf(it) }
            ?: parseMultipleResults(intent?.clipData?.takeIf { resultCode == Activity.RESULT_OK })
    }

    private fun parseMultipleResults(clipData: ClipData?): List<Uri> {
        return clipData?.let {
            val list = mutableListOf<Uri>()
            for (i in 0 until it.itemCount) {
                list += it.getItemAt(i).uri
            }
            return list
        } ?: emptyList()
    }
}
