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

package io.getstream.chat.android.ui.common.notifications

import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import io.getstream.chat.android.client.notifications.handler.UserIconBuilder
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader

/**
 * Produces an [IconCompat] using Coil, which downloads and caches the user image.
 */
@StreamHandsOff(
    reason = "This class shouldn't be renamed without verifying it works correctly on Chat Client Artifacts because " +
        "we are using it by reflection",
)
public class StreamCoilUserIconBuilder(private val context: Context) : UserIconBuilder {
    override suspend fun buildIcon(user: User): IconCompat? =
        StreamImageLoader
            .instance()
            .loadAsBitmap(context, user.image, StreamImageLoader.ImageTransformation.Circle)
            ?.let(IconCompat::createWithBitmap)
}
