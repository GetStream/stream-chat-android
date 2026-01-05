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

package io.getstream.chat.android.uitests.snapshot.uicomponents.components

import android.view.LayoutInflater
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.widgets.avatar.OnlineIndicatorPosition
import io.getstream.chat.android.ui.widgets.avatar.UserAvatarView
import io.getstream.chat.android.uitests.R
import io.getstream.chat.android.uitests.snapshot.uicomponents.UiComponentsScreenshotTest
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Test

class UserAvatarTest : UiComponentsScreenshotTest() {

    @Test
    fun uerAvatarForUserWithoutImage() {
        renderUserAvatarView(
            user = TestData.user1().copy(image = ""),
            onlineIndicatorEnabled = false,
        )
    }

    @Test
    fun uerAvatarForUserWithImage() {
        renderUserAvatarView(
            user = TestData.user1(),
            onlineIndicatorEnabled = false,
        )
    }

    @Test
    fun uerAvatarForOnlineUserWithTopEndIndicator() {
        renderUserAvatarView(user = TestData.user1())
    }

    @Test
    fun uerAvatarForOnlineUserWithBottomEndIndicator() {
        renderUserAvatarView(
            user = TestData.user1(),
            onlineIndicatorPosition = OnlineIndicatorPosition.BOTTOM_END,
        )
    }

    @Test
    fun uerAvatarForOnlineUserWithTopStartIndicator() {
        renderUserAvatarView(
            user = TestData.user1(),
            onlineIndicatorPosition = OnlineIndicatorPosition.TOP_START,
        )
    }

    @Test
    fun uerAvatarForOnlineUserWithBottomStartIndicator() {
        renderUserAvatarView(
            onlineIndicatorPosition = OnlineIndicatorPosition.BOTTOM_START,
            user = TestData.user1(),
        )
    }

    private fun renderUserAvatarView(
        user: User,
        onlineIndicatorEnabled: Boolean = true,
        onlineIndicatorPosition: OnlineIndicatorPosition = OnlineIndicatorPosition.TOP_END,
    ) {
        try {
            TransformStyle.avatarStyleTransformer = StyleTransformer { avatarStyle ->
                avatarStyle.copy(
                    onlineIndicatorEnabled = onlineIndicatorEnabled,
                    onlineIndicatorPosition = onlineIndicatorPosition,
                )
            }

            val userAvatarView = LayoutInflater
                .from(context)
                .inflate(R.layout.view_user_avatar, null, false) as UserAvatarView

            userAvatarView.setUser(user)

            compareScreenshot(view = userAvatarView, widthInPx = SCREENSHOT_SIZE, heightInPx = SCREENSHOT_SIZE)
        } finally {
            TransformStyle.avatarStyleTransformer = StyleTransformer { it }
        }
    }

    companion object {
        private const val SCREENSHOT_SIZE = 128
    }
}
