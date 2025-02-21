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

package io.getstream.chat.android.ui.common.disposable

/**
 * Wrapper around the Coil Disposable.
 *
 * @param disposable [coil3.request.Disposable]
 */
public class CoilDisposable(private val disposable: coil3.request.Disposable) : Disposable {

    override val isDisposed: Boolean
        get() = disposable.isDisposed

    /**
     * Dispose all the source. Use it when the resource is already used or the result is no longer needed.
     */
    override fun dispose() {
        disposable.dispose()
    }
}
