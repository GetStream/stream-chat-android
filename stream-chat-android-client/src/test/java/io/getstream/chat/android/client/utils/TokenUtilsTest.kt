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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [33])
internal class TokenUtilsTest(
    private val token: String,
    private val expectedUserId: String,
) {

    @Test
    fun `Should return userId inside of the token`() {
        TokenUtils.getUserId(token) `should be equal to` expectedUserId
    }

    companion object {

        @Suppress("MaxLineLength")
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}: {0} => {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken",
                "jc",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidmlzaGFsIn0=.devtoken",
                "vishal",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pbiJ9.devtoken",
                "amin",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWYzN2U1OGQtZDhiMC00NzZhLWE0ZjItZjg2MTFlMGQ4NWQ5In0.l3u9P1NKhJ91rI1tzOcABGh045Kj69-iVkC2yUtohVw",
                "1f37e58d-d8b0-476a-a4f2-f8611e0d85d9",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiRnJhIn0.ENQGHEsAL3WjVhd_qTiJa_9ojGKi2ftJ8xlocT8SVX4",
                "Fra",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNmQ5NTI3M2ItMzNmMC00MGY1LWIwN2MtMGRhMjYxMDkyMDc0In0.lT5O4EmWzhRKPTau6dHP4F6M42EA2aN_8-iAPuiFPLc",
                "6d95273b-33f0-40f5-b07c-0da261092074",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWUzMzAxMTEtNjcwZC00OWE3LThmMDgtZTY3MzQzMzhjNjQxIn0.YEFdEMWj5rurQKr0QMrvO72jGZHU-AlpUIbyY4jxYdU",
                "1e330111-670d-49a7-8f08-e6734338c641",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjllNDZkZWYtODhmNC00YjZhLWExMGMtNTg0ZDEwYzRmZGM5In0.Mxr4Prnb1-EVM5NSSP2EugLApSChoKnVFwe7ZO15V_U",
                "29e46def-88f4-4b6a-a10c-584d10c4fdc9",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWYwNTJjMDgtZjY4Mi00YTgzLTg5NmMtOWYxOWE2OGJkMmJiIn0.L-cQ-DYubOzFpsg94OEwlTRYjat9G4cqfAgzBPALW0g",
                "1f052c08-f682-4a83-896c-9f19a68bd2bb",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMGQzZTZlNjMtNjIwMC00ZGQxLWE4NDEtNDA1MDY2NDg5MWUyIn0.osFIgnle17f6yEkK7rPJguQaKhOiawAO3BylYaiRTqE",
                "0d3e6e63-6200-4dd1-a841-4050664891e2",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMTJmYjBlZDktOTNkOC00OGE1LTk4ODUtMjhlNDFmMmU0YzQzIn0.t_oc_DEwTav7ni0z4bi8Xla_5Zj5cI6l3rKxwoCvtB0",
                "12fb0ed9-93d8-48a5-9885-28e41f2e4c43",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNTUzMWE4Y2ItM2I4MS00YTU0LWI0MjQtN2FlNGUyN2JmOGJhIn0.PXkmukg3JU4igH_YUMr7WC7a1EcwKBr_C5V2ouBlmIs",
                "5531a8cb-3b81-4a54-b424-7ae4e27bf8ba",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMDYzNTY1NjQtMTQ5Zi00YjJjLTg1MjUtZDIyMDU2ZmVjNDA0In0.R3-HY9Cno62yIhCjLXDBR8LF7y1udwX8m4LLNP2dIZo",
                "06356564-149f-4b2c-8525-d22056fec404",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYWQ3ZDkzMTQtNTA3MS00ZDYxLTk4YTEtZmZhNjQzY2U4MjRhIn0.iF4UWGFtX0eTAIBTCum7fjD_TKn8wjEqb3PVxJrwbuM",
                "ad7d9314-5071-4d61-98a1-ffa643ce824a",
            ),
            arrayOf(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiY2ViZjU2MmEtNDgwNi00YzY0LWE4MjctNTlkNTBhYWM0MmJhIn0.kuXab7RhQRHdsErEW5tTN_mmuyLPNU4ZbprvuPXM4OY",
                "cebf562a-4806-4c64-a827-59d50aac42ba",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MCJ9.Vow00KvvhLvWRZIPKomXQOYpBL_P-_-eDeDKmBRvEj4",
                "qatest0",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MSJ9.H1nlYibjgp1HfaOd0sA_T4038tjsN61mJWxvUjmRQI0",
                "qatest1",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MiJ9.GYp9ikLtU2eG9Mq7tmHThzbV7C8W82j18sExuO7-ogc",
                "qatest2",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MyJ9.kLZJz5kl7e3Zw7i2T39Yp05_nAmh9RGG0rt6-5zOpfE",
                "qatest3",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.kLZJz5kl7e3Zw7i2T39Yp05_nAmh9RGG0rt6-5zOpfE",
                "",
            ),
            arrayOf(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.",
                "",
            ),
            arrayOf(
                randomString(),
                "",
            ),
            arrayOf(
                "${randomString()}.",
                "",
            ),
            arrayOf(
                "${randomString()}.${randomString()}",
                "",
            ),
            arrayOf(
                "",
                "",
            ),
        )
    }
}
