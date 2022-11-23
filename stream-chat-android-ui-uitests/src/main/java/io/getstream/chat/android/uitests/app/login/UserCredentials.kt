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
        user = User().apply {
            id = "jc"
            name = "Jc Miñarro"
            image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.WtWn2rgZyJNOpg48xUgYoMnG3BvMD5524RND7e7Mmhk"
    ),
    UserCredentials(
        user = User().apply {
            id = "amit"
            name = "Amit Kumar"
            image = "https://ca.slack-edge.com/T02RM6X6B-U027L4AMGQ3-9ca65ea80b60-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pdCJ9.nYq8v8uFvdMwo3GlhQj_prXZArhi1dol-nkhPJsPXjI"
    ),
    UserCredentials(
        user = User().apply {
            id = "belal"
            name = "Belal Khan"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02DAP0G2AV-2072330222dc-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVsYWwifQ.aGImcXURXHTJZuXpBflcu2U4bWZ84dRJfLkAR5NuTas"
    ),
    UserCredentials(
        user = User().apply {
            id = "dmitrii"
            name = "Dmitrii Bychkov"
            image = "https://ca.slack-edge.com/T02RM6X6B-U01CDPY6YE8-b74b0739493e-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG1pdHJpaSJ9.Mc0yYuNd-Kk_udd2tqiaOFbCnMRk8Lpq4JDDWi4X6CY"
    ),
    UserCredentials(
        user = User().apply {
            id = "filip"
            name = "Filip Babić"
            image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.ZKNNAq-i-oKINbgJVpb750F984gmINRk5eslKK5avA0"
    ),
    UserCredentials(
        user = User().apply {
            id = "jaewoong"
            name = "Jaewoong Eum"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02HU1XR9LM-626fb91c334e-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFld29vbmcifQ.SoSQ1zkdpD-xzrdTnNioFOZQ5pKKcLJj4Jw8wAzOhno"
    ),
    UserCredentials(
        user = User().apply {
            id = "kanat"
            name = "Kanat Kiialbaev"
            image = "https://ca.slack-edge.com/T02RM6X6B-U034NG4FPNG-688fab30cc42-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXQifQ.9DFWiJmdfOsUiYvURCYN5bpUOiMrDvZN62X7HLweVPo"
    ),
    UserCredentials(
        user = User().apply {
            id = "leandro"
            name = "Leandro Borges Ferreira"
            image = "https://ca.slack-edge.com/T02RM6X6B-U01AQ67NJ9Z-2f28d711cae9-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.HGQfqwt1Jj2W25QQLO8hbnNeOrUl04q4a_AjHy0sFWM"
    ),
    UserCredentials(
        user = User().apply {
            id = "marin"
            name = "Marin Tolić"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02N2HTP79A-645f7845aa22-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWFyaW4ifQ.jSLjDAcGcz7hHrDyIU8OaSgRDg63aw2cW4C1nqIsB5c"
    ),
    UserCredentials(
        user = User().apply {
            id = "rafal"
            name = "Rafal Adasiewicz"
            image = "https://ca.slack-edge.com/T02RM6X6B-U0177N46AFN-a4e664d1960d-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicmFmYWwifQ.Fx_BMBwlwbfZzSonziklhGt0KWTKVnLRtXRI5RIuJKU"
    ),
    UserCredentials(
        user = User().apply {
            id = "tomislav"
            name = "Tomislav Gazica"
            image = "https://ca.slack-edge.com/T02RM6X6B-U036AL9LV3Q-aabe5318d2aa-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9taXNsYXYifQ.-n5L1OmG4KpL0O16h8SeqKJID0DzUmy_nilqTgi8EZQ"
    ),
    UserCredentials(
        user = User().apply {
            id = "thierry"
            name = "Thierry Schellenbach"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02RM6X6D-g28a1278a98e-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidGhpZXJyeSJ9.pfG5thPeT7ytMSExrg3yoXQ02AqrLj2gIxuBZkTlr_c"
    ),
    UserCredentials(
        user = User().apply {
            id = "tommaso"
            name = "Tommaso Barbugli"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02U7SJP4-0f65a5997877-128"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.1H7yz3CZ_wtSxj0B36tOkfdFtKzULSPwRyesufrTGpM"
    ),
    UserCredentials(
        user = User().apply {
            id = "qatest1"
            name = "QA Test 1"
            image = "https://getstream.imgix.net/images/random_svg/QT.png"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MSJ9.T-LLXoVmdX1Huxg-gjUnBJ7TEnlyGSfa9zFw-N50zLY"
    ),
    UserCredentials(
        user = User().apply {
            id = "qatest2"
            name = "QA Test 2"
            image = "https://getstream.imgix.net/images/random_svg/QT.png"
        },
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MiJ9.EVNBLb7-yR823jv2jtDcjYBQCxBAQnhLDNhpMjcJ1uI"
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
