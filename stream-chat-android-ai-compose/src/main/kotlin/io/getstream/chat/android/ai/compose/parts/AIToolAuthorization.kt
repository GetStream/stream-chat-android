/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ai.compose.parts

import java.net.URI
import java.net.URISyntaxException
import java.util.Date
import java.util.Locale

/**
 * A sign-in a tool call waits for, such as "Connect Linear", and how it ended.
 *
 * The agent's backend writes it on the call's `ai_tool_call` step, which waits with status
 * `awaiting_authorization`, and holds the call until the person signs in at the provider in a
 * browser. Once they connect, the call goes on. When the sign-in is declined, expires or fails, the
 * call is cancelled and never runs. The step holds no credential, since [url] is the agent's own
 * sign-in page and the provider hands what it grants to the agent's backend, never to the app.
 *
 * @param kind How the person signs in. Defaults to [Kind.OAuth] when the agent sends none.
 * @param provider The provider, such as `linear`.
 * @param providerName What people call the provider, such as "Linear".
 * @param title The question, such as "Connect Linear".
 * @param message What signing in means for the person.
 * @param url The sign-in page to open: an `https` URL, or an `http` one on this device itself
 * (`localhost` or a loopback address), as a router run locally serves. Open [launchUrl], which
 * carries the [handoffToken].
 * @param handoffToken What binds the sign-in to the first browser that opens it. It goes in the
 * page's fragment, which no server sees. Gone once the sign-in is over.
 * @param expiresAt When the sign-in expires.
 * @param status How it ended, once it did.
 */
@Suppress("LongParameterList") // One property per field of the sign-in.
public data class AIToolAuthorization(
    val kind: Kind = Kind.OAuth,
    val provider: String,
    val providerName: String? = null,
    val title: String,
    val message: String? = null,
    val url: String,
    val handoffToken: String? = null,
    val expiresAt: Date? = null,
    val status: Status? = null,
) {

    /**
     * The sign-in with its [handoffToken] masked, so logging it, or a part that carries it, doesn't
     * leak a live token.
     */
    override fun toString(): String = "AIToolAuthorization(kind=$kind, provider=$provider, " +
        "providerName=$providerName, title=$title, message=$message, url=$url, " +
        "handoffToken=${handoffToken?.let { "<redacted>" }}, expiresAt=$expiresAt, status=$status)"

    /** The page to open in a browser, which is [url] with the [handoffToken] in its fragment. */
    public val launchUrl: String
        get() {
            val token = handoffToken ?: return url
            return url.substringBefore('#') + "#handoff_token=" + encodeFragmentValue(token)
        }

    /**
     * How the person signs in. An open set: compare against the kinds you know.
     *
     * @param rawValue The kind as the agent wrote it.
     */
    @JvmInline
    public value class Kind(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** A consent at the provider, in a browser (OAuth). */
            public val OAuth: Kind = Kind("oauth")
        }
    }

    /**
     * How a sign-in ended. An open set: compare against the statuses you know.
     *
     * @param rawValue The status as the agent's backend wrote it.
     */
    @JvmInline
    public value class Status(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** The person connected their account, so the call goes on. */
            public val Connected: Status = Status("connected")

            /** The person declined to sign in, so the call never ran. */
            public val Declined: Status = Status("declined")

            /** The sign-in expired before the person finished it. */
            public val Expired: Status = Status("expired")

            /**
             * The provider or the agent's backend ended it without connecting, such as when the
             * person came back as another account.
             */
            public val Failed: Status = Status("failed")
        }
    }

    internal companion object {
        /**
         * Reads the step's `authorization`. A sign-in with no title, or no secure web page to open,
         * asks nothing: the handoff token goes in the page's fragment, which a cleartext page would
         * expose to anything on the way.
         */
        fun from(fields: Fields): AIToolAuthorization? {
            val title = fields.string("title") ?: return null
            val url = fields.string("url")?.takeIf(::isOfferablePage) ?: return null
            return AIToolAuthorization(
                kind = fields.string("type")?.let(::Kind) ?: Kind.OAuth,
                provider = fields.string("provider").orEmpty(),
                providerName = fields.string("provider_name"),
                title = title,
                message = fields.string("message"),
                url = url,
                handoffToken = fields.string("handoff_token"),
                expiresAt = fields.date("expires_at"),
                status = fields.string("status")?.let(::Status),
            )
        }

        // The handoff token goes in the page's address, so it is offered only where nobody on the
        // way can read it: over https, or over http to this device itself, which never leaves it.
        private fun isOfferablePage(url: String): Boolean = try {
            val uri = URI(url)
            val host = uri.host
            when (uri.scheme?.lowercase(Locale.ROOT)) {
                SECURE_WEB_SCHEME -> !host.isNullOrEmpty()
                WEB_SCHEME -> host != null && isLoopback(host)
                else -> false
            }
        } catch (_: URISyntaxException) {
            false
        }

        private fun isLoopback(host: String): Boolean {
            val name = host.lowercase(Locale.ROOT).removePrefix("[").removeSuffix("]")
            return name == "localhost" || name == "::1" || LoopbackV4.matches(name)
        }

        // The page reads its fragment as form parameters, where `+` is a space and `&` and `=`
        // separate them, so a token carrying one of them is percent-encoded.
        private fun encodeFragmentValue(value: String): String = buildString {
            value.toByteArray(Charsets.UTF_8).forEach { byte ->
                val char = (byte.toInt() and BYTE_MASK).toChar()
                if ((char.code < ASCII_END && char.isLetterOrDigit()) || char in FragmentSafe) {
                    append(char)
                } else {
                    append('%').append(String.format(Locale.ROOT, "%02X", byte.toInt() and BYTE_MASK))
                }
            }
        }

        private const val SECURE_WEB_SCHEME = "https"
        private const val WEB_SCHEME = "http"
        private val LoopbackV4 = Regex("""127(\.\d{1,3}){3}""")
        private const val FragmentSafe = "!$'()*,-./:;?@_~"
        private const val BYTE_MASK = 0xFF
        private const val ASCII_END = 0x80
    }
}
