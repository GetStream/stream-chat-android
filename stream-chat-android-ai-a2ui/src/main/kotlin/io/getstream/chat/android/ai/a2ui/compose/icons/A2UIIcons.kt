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

package io.getstream.chat.android.ai.a2ui.compose.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The icons of the A2UI basic catalog, as Material icons, and icons the agent draws with an SVG
 * path.
 */
internal object A2UIIcons {

    private const val VIEWPORT = 24f

    private val icons: Map<String, () -> ImageVector> = mapOf(
        "accountCircle" to { Icons.Rounded.AccountCircle },
        "add" to { Icons.Rounded.Add },
        "arrowBack" to { Icons.AutoMirrored.Rounded.ArrowBack },
        "arrowForward" to { Icons.AutoMirrored.Rounded.ArrowForward },
        "attachFile" to { Icons.Rounded.AttachFile },
        "calendarToday" to { Icons.Rounded.CalendarToday },
        "call" to { Icons.Rounded.Call },
        "camera" to { Icons.Rounded.PhotoCamera },
        "check" to { Icons.Rounded.Check },
        "close" to { Icons.Rounded.Close },
        "delete" to { Icons.Rounded.Delete },
        "download" to { Icons.Rounded.Download },
        "edit" to { Icons.Rounded.Edit },
        "event" to { Icons.Rounded.EventAvailable },
        "error" to { Icons.Rounded.Error },
        "fastForward" to { path("fastForward", "M4 18l8.5-6L4 6v12zm9-12v12l8.5-6L13 6z") },
        "favorite" to { Icons.Rounded.Favorite },
        "favoriteOff" to { Icons.Rounded.FavoriteBorder },
        "folder" to { Icons.Rounded.Folder },
        "help" to { Icons.AutoMirrored.Rounded.Help },
        "home" to { Icons.Rounded.Home },
        "info" to { Icons.Rounded.Info },
        "locationOn" to { Icons.Rounded.LocationOn },
        "lock" to { Icons.Rounded.Lock },
        "lockOpen" to { Icons.Rounded.LockOpen },
        "mail" to { Icons.Rounded.Email },
        "menu" to { Icons.Rounded.Menu },
        "moreVert" to { Icons.Rounded.MoreVert },
        "moreHoriz" to { Icons.Rounded.MoreHoriz },
        "notificationsOff" to { Icons.Rounded.Notifications },
        "notifications" to { Icons.Rounded.Notifications },
        "pause" to { Icons.Rounded.Pause },
        "payment" to { Icons.Rounded.CreditCard },
        "person" to { Icons.Rounded.Person },
        "phone" to { Icons.Rounded.Phone },
        "photo" to { Icons.Rounded.Image },
        "play" to { Icons.Rounded.PlayArrow },
        "print" to { Icons.Rounded.Print },
        "refresh" to { Icons.Rounded.Refresh },
        "rewind" to { path("rewind", "M11 18V6l-8.5 6 8.5 6zm.5-6l8.5 6V6l-8.5 6z") },
        "search" to { Icons.Rounded.Search },
        "send" to { Icons.AutoMirrored.Rounded.Send },
        "settings" to { Icons.Rounded.Settings },
        "share" to { Icons.Rounded.Share },
        "shoppingCart" to { Icons.Rounded.ShoppingCart },
        "skipNext" to { path("skipNext", "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z") },
        "skipPrevious" to { path("skipPrevious", "M6 6h2v12H6zm3.5 6l8.5 6V6z") },
        "star" to { Icons.Rounded.Star },
        "starHalf" to { Icons.Rounded.Star },
        "starOff" to {
            path(
                "starOff",
                "M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24z" +
                    "M12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z",
            )
        },
        "stop" to { path("stop", "M6 6h12v12H6z") },
        "upload" to { Icons.Rounded.Upload },
        "visibility" to { Icons.Rounded.Visibility },
        "visibilityOff" to { Icons.Rounded.VisibilityOff },
        "volumeDown" to {
            path(
                "volumeDown",
                "M18.5 12c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM5 9v6h4l5 5V4L9 9H5z",
            )
        },
        "volumeMute" to { path("volumeMute", "M7 9v6h4l5 5V4l-5 5H7z") },
        "volumeOff" to { path("volumeOff", "M7 9v6h4l5 5V4l-5 5H7z") },
        "volumeUp" to { Icons.AutoMirrored.Rounded.VolumeUp },
        "warning" to { Icons.Rounded.Warning },
    )

    /** Every icon name of the basic catalog. */
    val names: Set<String> get() = icons.keys

    /** The icon for [name], or a neutral one for a name the catalog doesn't have. */
    fun vector(name: String?): ImageVector = name?.let(icons::get)?.invoke() ?: Icons.AutoMirrored.Rounded.Help

    /** An icon the agent draws with the SVG path [data] in a 24 by 24 box, or `null` when it doesn't parse. */
    fun fromSvgPath(data: String): ImageVector? = try {
        path("svgPath", data)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IndexOutOfBoundsException) {
        null
    } catch (_: NumberFormatException) {
        null
    }

    private fun path(name: String, data: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = VIEWPORT.dp,
        defaultHeight = VIEWPORT.dp,
        viewportWidth = VIEWPORT,
        viewportHeight = VIEWPORT,
    ).addPath(pathData = PathParser().parsePathString(data).toNodes(), fill = SolidColor(Color.Black)).build()
}
