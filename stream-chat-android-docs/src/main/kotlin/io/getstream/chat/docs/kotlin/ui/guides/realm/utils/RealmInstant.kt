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

package io.getstream.chat.docs.kotlin.ui.guides.realm.utils

import io.realm.kotlin.types.RealmInstant
import java.util.Date

private const val ONE_K: Int = 1000

internal fun RealmInstant.toDate() = Date(this.epochSeconds * ONE_K)

internal fun Date.toRealmInstant() = RealmInstant.from(time / ONE_K, 0)
