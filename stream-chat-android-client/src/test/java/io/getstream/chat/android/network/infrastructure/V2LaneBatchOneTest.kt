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

package io.getstream.chat.android.network.infrastructure

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.network.models.ChatPreferencesInput
import io.getstream.chat.android.network.models.CreateDeviceRequest
import io.getstream.chat.android.network.models.GetApplicationResponse
import io.getstream.chat.android.network.models.GetOGResponse
import io.getstream.chat.android.network.models.ListDevicesResponse
import io.getstream.chat.android.network.models.PushPreferenceInput
import io.getstream.chat.android.network.models.Response
import io.getstream.chat.android.network.models.SearchRolesResponse
import io.getstream.chat.android.network.models.UpsertPushPreferencesRequest
import io.getstream.chat.android.network.models.UpsertPushPreferencesResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The endpoints of the first v2 batch (app settings, roles, devices, push preferences, og) parse their recorded v2
 * responses through the generated [Serializer] alone, and send the same request bodies as on v1.
 */
internal class V2LaneBatchOneTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    @Test
    fun `App settings parse from v2`() {
        val app = with(mapping) { parse<GetApplicationResponse>(APP).toDomain() }.app

        assertEquals("Stream SDK - Android", app.name)
        assertEquals(listOf(".json"), app.fileUploadConfig.blockedFileExtensions)
    }

    @Test
    fun `Roles parse their integer dates from v2`() {
        val role = with(mapping) { parse<SearchRolesResponse>(ROLES).roles.single().toDomain() }

        assertEquals("admin", role.name)
        assertEquals(Date(1624960800000), role.createdAt)
        assertEquals(Date(1624960800000), role.updatedAt)
    }

    @Test
    fun `Devices parse from v2`() {
        val devices = with(mapping) { parse<ListDevicesResponse>(DEVICES).devices.map { it.toDomain() } }

        assertEquals(
            listOf(
                Device(token = "v2cap-device", pushProvider = PushProvider.FIREBASE, providerName = null),
                Device(token = "v2cap-device-2", pushProvider = PushProvider.FIREBASE, providerName = "chat-android-firebase"),
            ),
            devices,
        )
    }

    @Test
    fun `A bare duration response parses from v2`() {
        assertEquals("5.79ms", parse<Response>("""{"duration":"5.79ms"}""").duration)
    }

    @Test
    fun `Push preferences parse from v2`() {
        val response = parse<UpsertPushPreferencesResponse>(PUSH_PREFERENCES)

        assertEquals(
            "all",
            response.userChannelPreferences["filip"]?.get("messaging:sample-app-channel-8")?.chatLevel,
        )
    }

    @Test
    fun `An og scrape parses from v2 with its empty custom data`() {
        val attachment = with(mapping) { parse<GetOGResponse>(OG).toDomain() }

        assertEquals("https://getstream.io", attachment.ogUrl)
        assertEquals("https://getstream.io/assets/images/og-home-89a22fb4.jpg", attachment.imageUrl)
        assertEquals(emptyMap<String, Any>(), attachment.extraData)
    }

    @Test
    fun `A device request has the same body on v2 as on v1`() {
        assertSameBody(
            CreateDeviceRequest(
                id = "token",
                pushProvider = CreateDeviceRequest.PushProvider.fromString("firebase"),
                pushProviderName = null,
            ),
        )
    }

    @Test
    fun `A push preferences request has the same body on v2 as on v1`() {
        assertSameBody(
            UpsertPushPreferencesRequest(
                listOf(
                    PushPreferenceInput(
                        channelCid = "messaging:general",
                        chatLevel = PushPreferenceInput.ChatLevel.fromString("mentions"),
                        disabledUntil = Date(1791556406815),
                        removeDisable = null,
                        chatPreferences = ChatPreferencesInput(
                            directMentions = ChatPreferencesInput.DirectMentions.fromString("all"),
                        ),
                    ),
                ),
            ),
        )
    }

    private fun assertSameBody(request: Any) {
        val v2Body = Serializer.moshi.adapter(request.javaClass).toJson(request)
        assertEquals(v1Parser.toJson(request), v2Body)
    }

    private companion object {
        private const val APP = """{"app":{"id":102398,"name":"Stream SDK - Android","placement":"gcp-us-east4.c4",
            "async_url_enrich_enabled":false,"auto_translation_enabled":false,
            "file_upload_config":{"allowed_file_extensions":[],"blocked_file_extensions":[".json"],
            "allowed_mime_types":[],"blocked_mime_types":[],"size_limit":0},
            "image_upload_config":{"allowed_file_extensions":[],"blocked_file_extensions":[],"allowed_mime_types":[],
            "blocked_mime_types":[],"size_limit":0},"video_provider":""},"duration":"1.03ms"}"""

        private const val ROLES = """{"duration":"2.31ms","roles":[{"name":"admin","custom":false,
            "scopes":[".app","messaging"],"created_at":1624960800000000000,"updated_at":1624960800000000000}]}"""

        /** The second device's token is a placeholder for the recorded push token. */
        private const val DEVICES = """{"devices":[
            {"push_provider":"firebase","id":"v2cap-device","created_at":1791556406815008000,"user_id":"filip"},
            {"push_provider":"firebase","push_provider_name":"chat-android-firebase","id":"v2cap-device-2",
            "created_at":1791446274800107000,"user_id":"filip"}],"duration":"2.17ms"}"""

        private const val PUSH_PREFERENCES = """{"user_preferences":{"filip":null},
            "user_channel_preferences":{"filip":{"messaging:sample-app-channel-8":{"chat_level":"all"}}},
            "duration":"9.13ms"}"""

        private const val OG = """{"type":"image","title":"Scalable Chat, Video & Activity Feed APIs and SDKs | Stream",
            "title_link":"https://getstream.io/","text":"Scalable and fast APIs for building social networks and apps.",
            "image_url":"https://getstream.io/assets/images/og-home-89a22fb4.jpg",
            "thumb_url":"https://getstream.io/assets/images/og-home-89a22fb4.jpg","custom":{},
            "og_scrape_url":"https://getstream.io","duration":"2.20ms"}"""
    }
}
