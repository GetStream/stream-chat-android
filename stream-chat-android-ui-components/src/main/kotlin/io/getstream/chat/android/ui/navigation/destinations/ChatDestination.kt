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

package io.getstream.chat.android.ui.navigation.destinations

import android.app.Activity
import android.content.Context
import android.content.Intent

public abstract class ChatDestination(protected val context: Context) {
    public abstract fun navigate()

    protected fun start(intent: Intent) {
        context.startActivity(intent)
    }

    protected fun startForResult(intent: Intent, requestCode: Int) {
        check(context is Activity) {
            "startForResult can only be used if your destination uses an Activity as its Context"
        }
        context.startActivityForResult(intent, requestCode)
    }
}
