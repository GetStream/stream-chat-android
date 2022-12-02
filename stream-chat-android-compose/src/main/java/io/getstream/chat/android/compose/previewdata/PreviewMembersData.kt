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

package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.models.Member

/**
 * Provides sample members that will be used to render previews.
 */
internal object PreviewMembersData {

    private val member1: Member = Member(user = PreviewUserData.user1)
    private val member2: Member = Member(user = PreviewUserData.user2)
    private val member3: Member = Member(user = PreviewUserData.user3)
    private val member4: Member = Member(user = PreviewUserData.user4)

    val oneMember: List<Member> = listOf(member1)

    val manyMembers: List<Member> = listOf(
        member1,
        member2,
        member3,
        member4
    )
}
