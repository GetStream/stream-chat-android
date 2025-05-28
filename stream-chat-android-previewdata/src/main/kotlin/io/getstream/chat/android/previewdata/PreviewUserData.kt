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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.User

/**
 * Provides sample users that will be used to render component previews.
 */
public object PreviewUserData {

    /**
     * A collection of users with names and avatars.
     */
    public val user1: User = User(
        id = "jc",
        name = "Jc Miñarro",
        image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
    )
    public val user2: User = User(
        id = "leia_organa",
        name = "Leia Organa",
        image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
    )
    public val user3: User = User(
        id = "han_solo",
        name = "Han Solo",
        image = "https://vignette.wikia.nocookie.net/starwars/images/e/e2/TFAHanSolo.png",
    )
    public val user4: User = User(
        id = "lando_calrissian",
        name = "Lando Calrissian",
        image = "https://vignette.wikia.nocookie.net/starwars/images/8/8f/Lando_ROTJ.png",
    )
    public val user5: User = User(
        id = "chewbacca",
        name = "Chewbacca",
        image = "https://vignette.wikia.nocookie.net/starwars/images/4/48/Chewbacca_TLJ.png",
    )

    private val user6: User = User(
        id = "c-3po",
        name = "C-3PO",
        image = "https://vignette.wikia.nocookie.net/starwars/images/3/3f/C-3PO_TLJ_Card_Trader_Award_Card.png",
    )

    public val user7: User = User(
        id = "andrerego",
        name = "André Rêgo",
        image = "https://ca.slack-edge.com/T02RM6X6B-U083JCB6ZEY-2da235988b74-512",
    )

    /**
     * Users with specific properties.
     */

    public val userWithImage: User = user1
    public val userWithOnlineStatus: User = user2.copy(online = true)
    public val userWithoutImage: User = user6.copy(image = "")
}
