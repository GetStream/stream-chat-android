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

package io.getstream.chat.android.compose.sample.data

/**
 * The US East staging app, where features are turned on before they reach production.
 */
object StagingEnvironment {

    /** The API key of the staging app. */
    const val API_KEY: String = "vfa6gxospjf0"

    /** The base URL of the staging app. */
    const val BASE_URL: String = "chat-edge-us-east1-ce1.gcp.stream-io-api.com"

    /** The base URL of the app with [apiKey], or null for the default one. */
    fun baseUrlOf(apiKey: String): String? = BASE_URL.takeIf { apiKey == API_KEY }
}
