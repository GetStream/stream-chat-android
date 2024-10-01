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

package io.getstream.chat.ui.sample.application

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.ui.sample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "qx5us2v6xvmh"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    val availableUsers: List<SampleUser> = listOf(
        SampleUser(
            apiKey = apiKey,
            id = "aapostol",
            name = "Aleksandar Apostolov",
            image = "https://ca.slack-edge.com/T02RM6X6B-U05UD37MA1G-f062f8b7afc2-72",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYWFwb3N0b2wifQ.kjeSx_Zyj84NDl37FLuEiEgZEdfpa4AKfhRFkonPP9A",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "dnovak",
            name = "Daniel Novak",
            image = "https://ca.slack-edge.com/T02RM6X6B-U05DKELFBB8-a8641b819de8-192",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG5vdmFrIn0.HuUyFkiXjHRk7hk4g2FLOg0szEi5Zq1u6CRC9t2Mwj8",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "jc",
            name = "Jc Miñarro",
            image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ.2_5Hae3LKjVSfA0gQxXlZn54Bq6xDlhjPx2J7azUNB4",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "amit",
            name = "Amit Kumar",
            image = "https://ca.slack-edge.com/T02RM6X6B-U027L4AMGQ3-9ca65ea80b60-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pdCJ9.MNfrDsGkFINEZ3kCQ9hAqI38lZ6S-miHINAuH3kQy2o",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "belal",
            name = "Belal Khan",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02DAP0G2AV-2072330222dc-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYmVsYWwifQ.a0DwMMb0V1Lona_1dIB7a4GtNl4oQ_WCp-W-UP3_CUQ",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "dmitrii",
            name = "Dmitrii Bychkov",
            image = "https://ca.slack-edge.com/T02RM6X6B-U01CDPY6YE8-b74b0739493e-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZG1pdHJpaSJ9._j7pM2kqj46ztls0tG1DiUMl45l54VOLvl8jp5VCmZU",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "filip",
            name = "Filip Babić",
            image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.WKqTjU6fHHjtFej-sUqS2ml3Rvdqn4Ptrf7jfKqzFgU",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "jaewoong",
            name = "Jaewoong Eum",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02HU1XR9LM-626fb91c334e-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamFld29vbmcifQ.d-7AREGaSirn7TjxwLyAUvOU-nz2_LL5oMTycZvcnQc",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "leandro",
            name = "Leandro Borges Ferreira",
            image = "https://ca.slack-edge.com/T02RM6X6B-U01AQ67NJ9Z-2f28d711cae9-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibGVhbmRybyJ9.CjlYUr79r4GopAhXIbqLBighl3meLsT4dQKzdKX7L3g",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "marton",
            name = "Márton Braun",
            image = "https://ca.slack-edge.com/T02RM6X6B-U018YPHEW7L-26ab96fd1ed3-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibWFydG9uIn0.22wjzwYCNdaG5FLVeTD49NqVA11UJpEwrNRjZxZrcK8",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "oleg",
            name = "Oleg Kuzmin",
            image = "https://ca.slack-edge.com/T02RM6X6B-U019BEATNCD-bad2dcf654ef-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib2xlZyJ9.ZucjlxjiNewCORdCLwpKwZw2nNtRC_Bv17TjHlitdLU",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "rafal",
            name = "Rafal Adasiewicz",
            image = "https://ca.slack-edge.com/T02RM6X6B-U0177N46AFN-a4e664d1960d-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicmFmYWwifQ.7Y4QCvc42Km8ETLdCQT5ynjiKVbZZbuN0XTiGxJNU6k",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "samuel",
            name = "Samuel Urbanowicz",
            image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXD396-6d3169b36889-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic2FtdWVsIn0.SusttZNc2Y0sc-JPEOPCmTa5FuKDHRcWGO_7kYrC1C0",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "thierry",
            name = "Thierry Schellenbach",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02RM6X6D-g28a1278a98e-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidGhpZXJyeSJ9.hZi4pBPt2v2HSoS-7Yn7Ll2a1twhs763MlRGFAday2c",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "tommaso",
            name = "Tommaso Barbugli",
            image = "https://ca.slack-edge.com/T02RM6X6B-U02U7SJP4-0f65a5997877-128",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidG9tbWFzbyJ9.lNaWC2Opyq6gmV50a2BGxK-5gm5mwCpefnUA30_k9YA",
            language = "en",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "kanat",
            name = "Kanat",
            image = "https://ca.slack-edge.com/T02RM6X6B-U034NG4FPNG-688fab30cc42-72",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXQifQ.MVoS7rCos7o3D7fkUcCFHVThKrN0sAaENupmXHYX3vw",
            language = "fr",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "kanat_ninja",
            name = "Kanat Ninja",
            image = "https://getstream.io/static/a4ba18b7dc1eedfa3ea4edbac74ce5e4/a3911/kanat-kiialbaev.webp",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoia2FuYXRfbmluamEifQ.s6We-MRy0ZXraiyLz8kShEPPIxXfqwUOOClfJgpTk8c",
            privacySettings = PrivacySettings(
                typingIndicators = TypingIndicators(enabled = false),
                readReceipts = ReadReceipts(enabled = false),
            ),
        ),
        SampleUser(
            apiKey = apiKey,
            id = "pvelikov",
            name = "Petar Velikov",
            image = "https://ca.slack-edge.com/T02RM6X6B-U07LDJZRUTG-a4129fed05b6-512",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicHZlbGlrb3YifQ.d5eenuTIZD5gZh7rHiv3lYbE8uOqUiHfwULtUr8a-l0",
            language = "en"
        ),
        SampleUser(
            apiKey = apiKey,
            id = "qatest1",
            name = "QA Test 1",
            image = "https://getstream.imgix.net/images/random_svg/QT.png",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MSJ9.H1nlYibjgp1HfaOd0sA_T4038tjsN61mJWxvUjmRQI0",
        ),
        SampleUser(
            apiKey = apiKey,
            id = "qatest2",
            name = "QA Test 2",
            image = "https://getstream.imgix.net/images/random_svg/QT.png",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MiJ9.GYp9ikLtU2eG9Mq7tmHThzbV7C8W82j18sExuO7-ogc",
        ),
    )
}
