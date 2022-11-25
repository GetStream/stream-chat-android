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

package io.getstream.chat.android.client.api2.optimisation.hash

import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter
import java.util.Date

internal data class QueryBanedUsersHash(
    val filter: FilterObject,
    val sort: QuerySorter<BannedUsersSort>,
    val offset: Int?,
    val limit: Int?,
    val createdAtAfter: Date?,
    val createdAtAfterOrEqual: Date?,
    val createdAtBefore: Date?,
    val createdAtBeforeOrEqual: Date?,
)
