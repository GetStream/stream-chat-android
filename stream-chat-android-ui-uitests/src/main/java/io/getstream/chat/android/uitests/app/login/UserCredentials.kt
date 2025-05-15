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

package io.getstream.chat.android.uitests.app.login

import io.getstream.chat.android.models.User

/**
 * A predefined set of available users.
 */
val userCredentialsList: List<UserCredentials> = listOf(
    UserCredentials(
        user = User(
            id = "jc",
            name = "Jc Miñarro",
            image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.WtWn2rgZyJNOpg48xUgYoMnG3BvMD5524RND7e7Mmhk",
    ),

    UserCredentials(
        user = User(
            id = "thierry",
            name = "Thierry Schellenbach",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02RM6X6D-g28a1278a98e-128",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidGhpZXJyeSJ9.pfG5thPeT7ytMSExrg3yoXQ02AqrLj2gIxuBZkTlr_c",
    ),
    UserCredentials(
        user = User(
            id = "tommaso",
            name = "Tommaso Barbugli",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02U7SJP4-0f65a5997877-128",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.1H7yz3CZ_wtSxj0B36tOkfdFtKzULSPwRyesufrTGpM",
    ),
    UserCredentials(
        user = User(
            id = "amit",
            name = "Leia Organa",
            image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pdCJ9.nYq8v8uFvdMwo3GlhQj_prXZArhi1dol-nkhPJsPXjI",
    ),
    UserCredentials(
        user = User(
            id = "belal",
            name = "Han Solo",
            image = "https://vignette.wikia.nocookie.net/starwars/images/e/e2/TFAHanSolo.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVsYWwifQ.aGImcXURXHTJZuXpBflcu2U4bWZ84dRJfLkAR5NuTas",
    ),
    UserCredentials(
        user = User(
            id = "dmitrii",
            name = "Lando Calrissian",
            image = "https://vignette.wikia.nocookie.net/starwars/images/8/8f/Lando_ROTJ.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG1pdHJpaSJ9.Mc0yYuNd-Kk_udd2tqiaOFbCnMRk8Lpq4JDDWi4X6CY",
    ),
    UserCredentials(
        user = User(
            id = "filip",
            name = "Chewbacca",
            image = "https://vignette.wikia.nocookie.net/starwars/images/4/48/Chewbacca_TLJ.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.ZKNNAq-i-oKINbgJVpb750F984gmINRk5eslKK5avA0",
    ),
    UserCredentials(
        user = User(
            id = "jaewoong",
            name = "C-3PO",
            image = "https://vignette.wikia.nocookie.net/starwars/images/3/3f/C-3PO_TLJ_Card_Trader_Award_Card.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFld29vbmcifQ.SoSQ1zkdpD-xzrdTnNioFOZQ5pKKcLJj4Jw8wAzOhno",
    ),
    UserCredentials(
        user = User(
            id = "kanat",
            name = "Kanat Kiialbaev",
            image = "https://ca.slack-edge.com/T02RM6X6B-U034NG4FPNG-688fab30cc42-128",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXQifQ.9DFWiJmdfOsUiYvURCYN5bpUOiMrDvZN62X7HLweVPo",
    ),
    UserCredentials(
        user = User(
            id = "leandro",
            name = "Padmé Amidala",
            image = "https://vignette.wikia.nocookie.net/starwars/images/b/b2/Padmegreenscrshot.jpg",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.HGQfqwt1Jj2W25QQLO8hbnNeOrUl04q4a_AjHy0sFWM",
    ),
    UserCredentials(
        user = User(
            id = "rafal",
            name = "Anakin Skywalker",
            image = "https://vignette.wikia.nocookie.net/starwars/images/6/6f/Anakin_Skywalker_RotS.png",
        ),
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicmFmYWwifQ.Fx_BMBwlwbfZzSonziklhGt0KWTKVnLRtXRI5RIuJKU",
    ),
)

/**
 * A data class that encapsulates all the information needed to initialize
 * the SDK and connect to Stream servers.
 */
data class UserCredentials(
    val user: User,
    val token: String,
)
