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

package io.getstream.chat.android.compose.ui.messages.attachments.media

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.internal.file.ShareableUriProvider
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CaptureMediaLauncherTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var launcher: CaptureMediaLauncher

    @Test
    fun `launch does nothing when no camera app is installed`() {
        setContent(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        launch()

        launcher.isPickingTarget.shouldBeFalse()
        startedActivity().shouldBeNull()
    }

    @Test
    fun `launch opens the only camera app without asking`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        setContent(CaptureMediaContract.Mode.PHOTO)

        launch()

        launcher.isPickingTarget.shouldBeFalse()
        startedActivity()?.component?.className shouldBeEqualTo "PhotoActivity"
    }

    @Test
    fun `launch asks the user when several camera apps are available`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        setContent(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        launch()

        composeTestRule.onNodeWithText("VideoActivity").assertExists()
        startedActivity().shouldBeNull()
    }

    @Test
    fun `tapping a camera app opens it and closes the sheet`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        setContent(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        launch()

        composeTestRule.onNodeWithText("VideoActivity").performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        launcher.isPickingTarget.shouldBeFalse()
        startedActivity()?.component?.className shouldBeEqualTo "VideoActivity"
    }

    @Test
    fun `dismissing the sheet stops asking`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        setContent(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        launch()
        launcher.isPickingTarget.shouldBeTrue()

        composeTestRule.runOnIdle { ShadowDialog.getLatestDialog().onBackPressed() }
        composeTestRule.waitForIdle()

        launcher.isPickingTarget.shouldBeFalse()
        composeTestRule.onNodeWithText("VideoActivity").assertDoesNotExist()
        startedActivity().shouldBeNull()
    }

    private fun setContent(mode: CaptureMediaContract.Mode) {
        composeTestRule.setContent {
            ChatTheme {
                val contract = remember {
                    // FileProvider rejects Robolectric's file paths.
                    val uriProvider = mock<ShareableUriProvider> {
                        on { getUriForFile(any(), any()) } doReturn Uri.EMPTY
                    }
                    CaptureMediaContract(mode, uriProvider = uriProvider)
                }
                val activityLauncher = rememberLauncherForActivityResult(contract) {}
                val pickingTarget = remember { mutableStateOf(false) }
                val context = LocalContext.current
                launcher = remember { CaptureMediaLauncher(contract, activityLauncher, context, pickingTarget) }
                CaptureMediaTargetSheet(launcher)
            }
        }
    }

    private fun launch() {
        composeTestRule.runOnIdle { launcher.launch() }
        composeTestRule.waitForIdle()
    }

    private fun startedActivity(): Intent? =
        shadowOf(composeTestRule.activity).nextStartedActivityForResult?.intent

    private fun registerCameraActivity(action: String, name: String) {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.camera"
                this.name = name
            }
        }
        shadowOf(composeTestRule.activity.packageManager).addResolveInfoForIntent(Intent(action), resolveInfo)
    }
}
