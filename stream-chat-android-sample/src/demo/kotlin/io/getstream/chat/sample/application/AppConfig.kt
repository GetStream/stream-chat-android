package io.getstream.chat.sample.application

import io.getstream.chat.sample.data.user.SampleUser

object AppConfig {
    const val apiKey: String = "qx5us2v6xvmh"
    const val apiUrl: String = "chat-us-east-staging.stream-io-api.com"
    const val apiTimeout: Int = 6000
    const val cndTimeout: Int = 30000

    val availableUsers: List<SampleUser> = listOf(
        SampleUser(
            name = "Jc",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FJc.png?alt=media",
            id = "1f37e58d-d8b0-476a-a4f2-f8611e0d85d9",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWYzN2U1OGQtZDhiMC00NzZhLWE0ZjItZjg2MTFlMGQ4NWQ5In0.l3u9P1NKhJ91rI1tzOcABGh045Kj69-iVkC2yUtohVw"
        ),
        SampleUser(
            name = "Fra",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FFra.png?alt=media",
            id = "Fra",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiRnJhIn0.ENQGHEsAL3WjVhd_qTiJa_9ojGKi2ftJ8xlocT8SVX4"
        ),
        SampleUser(
            name = "Carter",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FCarter.png?alt=media",
            id = "6d95273b-33f0-40f5-b07c-0da261092074",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNmQ5NTI3M2ItMzNmMC00MGY1LWIwN2MtMGRhMjYxMDkyMDc0In0.lT5O4EmWzhRKPTau6dHP4F6M42EA2aN_8-iAPuiFPLc"
        ),
        SampleUser(
            name = "Dmitrii",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FDmitrii.png?alt=media",
            id = "1e330111-670d-49a7-8f08-e6734338c641",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWUzMzAxMTEtNjcwZC00OWE3LThmMDgtZTY3MzQzMzhjNjQxIn0.YEFdEMWj5rurQKr0QMrvO72jGZHU-AlpUIbyY4jxYdU"
        ),
        SampleUser(
            name = "Leandro",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FLeandro.png?alt=media",
            id = "29e46def-88f4-4b6a-a10c-584d10c4fdc9",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMjllNDZkZWYtODhmNC00YjZhLWExMGMtNTg0ZDEwYzRmZGM5In0.Mxr4Prnb1-EVM5NSSP2EugLApSChoKnVFwe7ZO15V_U"
        ),
        SampleUser(
            name = "Marton",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FMarton.png?alt=media",
            id = "1f052c08-f682-4a83-896c-9f19a68bd2bb",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMWYwNTJjMDgtZjY4Mi00YTgzLTg5NmMtOWYxOWE2OGJkMmJiIn0.L-cQ-DYubOzFpsg94OEwlTRYjat9G4cqfAgzBPALW0g"
        ),
        SampleUser(
            name = "Oleg",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FOleg.png?alt=media",
            id = "0d3e6e63-6200-4dd1-a841-4050664891e2",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMGQzZTZlNjMtNjIwMC00ZGQxLWE4NDEtNDA1MDY2NDg5MWUyIn0.osFIgnle17f6yEkK7rPJguQaKhOiawAO3BylYaiRTqE"
        ),
        SampleUser(
            name = "Rafal",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FRafal.png?alt=media",
            id = "12fb0ed9-93d8-48a5-9885-28e41f2e4c43",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMTJmYjBlZDktOTNkOC00OGE1LTk4ODUtMjhlNDFmMmU0YzQzIn0.t_oc_DEwTav7ni0z4bi8Xla_5Zj5cI6l3rKxwoCvtB0"
        ),
        SampleUser(
            name = "Samuel",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FSam.png?alt=media",
            id = "5531a8cb-3b81-4a54-b424-7ae4e27bf8ba",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNTUzMWE4Y2ItM2I4MS00YTU0LWI0MjQtN2FlNGUyN2JmOGJhIn0.PXkmukg3JU4igH_YUMr7WC7a1EcwKBr_C5V2ouBlmIs"
        ),
        SampleUser(
            name = "Tommaso",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FTommaso.png?alt=media",
            id = "06356564-149f-4b2c-8525-d22056fec404",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMDYzNTY1NjQtMTQ5Zi00YjJjLTg1MjUtZDIyMDU2ZmVjNDA0In0.R3-HY9Cno62yIhCjLXDBR8LF7y1udwX8m4LLNP2dIZo"
        ),
        SampleUser(
            name = "Thierry",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FThierry.png?alt=media",
            id = "ad7d9314-5071-4d61-98a1-ffa643ce824a",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYWQ3ZDkzMTQtNTA3MS00ZDYxLTk4YTEtZmZhNjQzY2U4MjRhIn0.iF4UWGFtX0eTAIBTCum7fjD_TKn8wjEqb3PVxJrwbuM"
        ),
        SampleUser(
            name = "Zetra",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FZetra.png?alt=media",
            id = "cebf562a-4806-4c64-a827-59d50aac42ba",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiY2ViZjU2MmEtNDgwNi00YzY0LWE4MjctNTlkNTBhYWM0MmJhIn0.kuXab7RhQRHdsErEW5tTN_mmuyLPNU4ZbprvuPXM4OY"
        ),
        SampleUser(
            name = "Qatest0",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FQatest0.png?alt=media",
            id = "qatest0",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MCJ9.Vow00KvvhLvWRZIPKomXQOYpBL_P-_-eDeDKmBRvEj4"
        ),
        SampleUser(
            name = "Qatest1",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FQatest1.png?alt=media",
            id = "qatest1",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MSJ9.H1nlYibjgp1HfaOd0sA_T4038tjsN61mJWxvUjmRQI0"
        ),
        SampleUser(
            name = "Qatest2",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FQatest2.png?alt=media",
            id = "qatest2",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MiJ9.GYp9ikLtU2eG9Mq7tmHThzbV7C8W82j18sExuO7-ogc"
        ),
        SampleUser(
            name = "Qatest3",
            image = "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FQatest3.png?alt=media",
            id = "qatest3",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoicWF0ZXN0MyJ9.kLZJz5kl7e3Zw7i2T39Yp05_nAmh9RGG0rt6-5zOpfE"
        ),
    )
}
