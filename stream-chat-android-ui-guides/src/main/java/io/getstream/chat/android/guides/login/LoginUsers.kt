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

package io.getstream.chat.android.guides.login

import io.getstream.chat.android.models.User

/**
 * Contains hardcoded [LoginUser] for the demo environment.
 */
object LoginUsers {

    fun createUsers(): List<LoginUser> {
        return listOf(
            LoginUser(
                user = User(
                    id = "jc",
                    name = "Jc Miñarro",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.2_5Hae3LKjVSfA0gQxXlZn54Bq6xDlhjPx2J7azUNB4",
            ),
            LoginUser(
                user = User(
                    id = "thierry",
                    name = "Thierry Schellenbach",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02RM6X6D-g28a1278a98e-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidGhpZXJyeSJ9.hZi4pBPt2v2HSoS-7Yn7Ll2a1twhs763MlRGFAday2c",
            ),
            LoginUser(
                user = User(
                    id = "tommaso",
                    name = "Tommaso Barbugli",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02U7SJP4-0f65a5997877-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.lNaWC2Opyq6gmV50a2BGxK-5gm5mwCpefnUA30_k9YA",
            ),
            LoginUser(
                user = User(
                    id = "kanat",
                    name = "Kanat",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U034NG4FPNG-688fab30cc42-72",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXQifQ.MVoS7rCos7o3D7fkUcCFHVThKrN0sAaENupmXHYX3vw",
            ),
            LoginUser(
                user = User(
                    id = "amit",
                    name = "Leia Organa",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pdCJ9.MNfrDsGkFINEZ3kCQ9hAqI38lZ6S-miHINAuH3kQy2o",
            ),
            LoginUser(
                user = User(
                    id = "belal",
                    name = "Han Solo",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/e/e2/TFAHanSolo.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVsYWwifQ.a0DwMMb0V1Lona_1dIB7a4GtNl4oQ_WCp-W-UP3_CUQ",
            ),
            LoginUser(
                user = User(
                    id = "dmitrii",
                    name = "Lando Calrissian",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/8/8f/Lando_ROTJ.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG1pdHJpaSJ9._j7pM2kqj46ztls0tG1DiUMl45l54VOLvl8jp5VCmZU",
            ),
            LoginUser(
                user = User(
                    id = "filip",
                    name = "Chewbacca",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/4/48/Chewbacca_TLJ.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.WKqTjU6fHHjtFej-sUqS2ml3Rvdqn4Ptrf7jfKqzFgU",
            ),
            LoginUser(
                user = User(
                    id = "jaewoong",
                    name = "C-3PO",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/3/3f/" +
                        "C-3PO_TLJ_Card_Trader_Award_Card.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFld29vbmcifQ.d-7AREGaSirn7TjxwLyAUvOU-nz2_LL5oMTycZvcnQc",
            ),
            LoginUser(
                user = User(
                    id = "oleg",
                    name = "R2-D2",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/e/eb/ArtooTFA2-Fathead.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib2xlZyJ9.ZucjlxjiNewCORdCLwpKwZw2nNtRC_Bv17TjHlitdLU",
            ),
            LoginUser(
                user = User(
                    id = "rafal",
                    name = "Anakin Skywalker",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/6/6f/Anakin_Skywalker_RotS.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicmFmYWwifQ.7Y4QCvc42Km8ETLdCQT5ynjiKVbZZbuN0XTiGxJNU6k",
            ),
            LoginUser(
                user = User(
                    id = "samuel",
                    name = "Obi-Wan Kenobi",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/4/4e/ObiWanHS-SWE.jpg",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic2FtdWVsIn0.SusttZNc2Y0sc-JPEOPCmTa5FuKDHRcWGO_7kYrC1C0",
            ),
            LoginUser(
                user = User(
                    id = "leandro",
                    name = "Padmé Amidala",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/b/b2/Padmegreenscrshot.jpg",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.CjlYUr79r4GopAhXIbqLBighl3meLsT4dQKzdKX7L3g",
            ),
            LoginUser(
                user = User(
                    id = "marton",
                    name = "Qui-Gon Jinn",
                    image = "https://vignette.wikia.nocookie.net/starwars/images/f/f6/Qui-Gon_Jinn_Headshot_TPM.jpg",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWFydG9uIn0.22wjzwYCNdaG5FLVeTD49NqVA11UJpEwrNRjZxZrcK8",
            ),
        )
    }
}
