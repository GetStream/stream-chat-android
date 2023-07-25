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
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.2_5Hae3LKjVSfA0gQxXlZn54Bq6xDlhjPx2J7azUNB4"
            ),
            LoginUser(
                user = User(
                    id = "amit",
                    name = "Amit Kumar",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U027L4AMGQ3-9ca65ea80b60-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pdCJ9.MNfrDsGkFINEZ3kCQ9hAqI38lZ6S-miHINAuH3kQy2o"
            ),
            LoginUser(
                user = User(
                    id = "belal",
                    name = "Belal Khan",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02DAP0G2AV-2072330222dc-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVsYWwifQ.a0DwMMb0V1Lona_1dIB7a4GtNl4oQ_WCp-W-UP3_CUQ"
            ),
            LoginUser(
                user = User(
                    id = "dmitrii",
                    name = "Dmitrii Bychkov",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U01CDPY6YE8-b74b0739493e-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG1pdHJpaSJ9._j7pM2kqj46ztls0tG1DiUMl45l54VOLvl8jp5VCmZU"
            ),
            LoginUser(
                user = User(
                    id = "filip",
                    name = "Filip Babić",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.WKqTjU6fHHjtFej-sUqS2ml3Rvdqn4Ptrf7jfKqzFgU"
            ),
            LoginUser(
                user = User(
                    id = "jaewoong",
                    name = "Jaewoong Eum",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02HU1XR9LM-626fb91c334e-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFld29vbmcifQ.d-7AREGaSirn7TjxwLyAUvOU-nz2_LL5oMTycZvcnQc"
            ),
            LoginUser(
                user = User(
                    id = "leandro",
                    name = "Leandro Borges Ferreira",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U01AQ67NJ9Z-2f28d711cae9-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.CjlYUr79r4GopAhXIbqLBighl3meLsT4dQKzdKX7L3g"
            ),
            LoginUser(
                user = User(
                    id = "marton",
                    name = "Márton Braun",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U018YPHEW7L-26ab96fd1ed3-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWFydG9uIn0.22wjzwYCNdaG5FLVeTD49NqVA11UJpEwrNRjZxZrcK8"
            ),
            LoginUser(
                user = User(
                    id = "oleg",
                    name = "Oleg Kuzmin",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U019BEATNCD-bad2dcf654ef-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib2xlZyJ9.ZucjlxjiNewCORdCLwpKwZw2nNtRC_Bv17TjHlitdLU"
            ),
            LoginUser(
                user = User(
                    id = "rafal",
                    name = "Rafal Adasiewicz",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U0177N46AFN-a4e664d1960d-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicmFmYWwifQ.7Y4QCvc42Km8ETLdCQT5ynjiKVbZZbuN0XTiGxJNU6k"
            ),
            LoginUser(
                user = User(
                    id = "samuel",
                    name = "Samuel Urbanowicz",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXD396-6d3169b36889-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic2FtdWVsIn0.SusttZNc2Y0sc-JPEOPCmTa5FuKDHRcWGO_7kYrC1C0"
            ),
            LoginUser(
                user = User(
                    id = "thierry",
                    name = "Thierry Schellenbach",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02RM6X6D-g28a1278a98e-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidGhpZXJyeSJ9.hZi4pBPt2v2HSoS-7Yn7Ll2a1twhs763MlRGFAday2c"
            ),
            LoginUser(
                user = User(
                    id = "tommaso",
                    name = "Tommaso Barbugli",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U02U7SJP4-0f65a5997877-128",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.lNaWC2Opyq6gmV50a2BGxK-5gm5mwCpefnUA30_k9YA"
            ),
            LoginUser(
                user = User(
                    id = "kanat",
                    name = "Kanat",
                    image = "https://ca.slack-edge.com/T02RM6X6B-U034NG4FPNG-688fab30cc42-72",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXQifQ.MVoS7rCos7o3D7fkUcCFHVThKrN0sAaENupmXHYX3vw"
            ),
            LoginUser(
                user = User(
                    id = "qatest1",
                    name = "QA Test 1",
                    image = "https://getstream.imgix.net/images/random_svg/QT.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MSJ9.H1nlYibjgp1HfaOd0sA_T4038tjsN61mJWxvUjmRQI0"
            ),
            LoginUser(
                user = User(
                    id = "qatest2",
                    name = "QA Test 2",
                    image = "https://getstream.imgix.net/images/random_svg/QT.png",
                ),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MiJ9.GYp9ikLtU2eG9Mq7tmHThzbV7C8W82j18sExuO7-ogc"
            ),
        )
    }
}
