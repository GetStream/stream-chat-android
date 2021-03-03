package io.getstream.chat.android.livedata.mockwebserver

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.github.cdimascio.dotenv.dotenv

internal class MockWebServerTestDataHelper {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val apiKey = checkNotNull(dotenv["STREAM_API_KEY"]) { "Be sure to specify the STREAM_API_KEY environment variable" }
    val logLevel =
        checkNotNull(dotenv["STREAM_LOG_LEVEL"]) { "Be sure to specify the STREAM_LOG_LEVEL environment variable" }

    val token =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiY2ViZjU2MmEtNDgwNi00YzY0LWE4MjctNTlkNTBhYWM0MmJhIn0.kuXab7RhQRHdsErEW5tTN_mmuyLPNU4ZbprvuPXM4OY"

    val user = User().apply {
        id = "cebf562a-4806-4c64-a827-59d50aac42ba"
        image =
            "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FZetra.png?alt=media"
        name = "Zetra"
    }
}
