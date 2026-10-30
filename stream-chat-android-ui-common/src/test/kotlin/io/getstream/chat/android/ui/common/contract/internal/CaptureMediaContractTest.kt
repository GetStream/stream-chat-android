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

package io.getstream.chat.android.ui.common.contract.internal

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CaptureMediaContractTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val outputUri = Uri.parse("content://provider/capture")

    @Test
    fun `targets lists photo apps before video apps in photo and video mode`() {
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        val sut = contract(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        val targets = sut.targets(context)

        targets.map { it.kind to it.activityInfo.name } shouldBeEqualTo listOf(
            CaptureMediaContract.Target.Kind.PHOTO to "PhotoActivity",
            CaptureMediaContract.Target.Kind.VIDEO to "VideoActivity",
        )
    }

    @Test
    fun `targets lists only the apps for the mode`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        val sut = contract(CaptureMediaContract.Mode.VIDEO)

        val targets = sut.targets(context)

        targets.map { it.kind to it.activityInfo.name } shouldBeEqualTo listOf(
            CaptureMediaContract.Target.Kind.VIDEO to "VideoActivity",
        )
    }

    @Test
    fun `targets is empty when no camera app is installed`() {
        val sut = contract(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        sut.targets(context) shouldBeEqualTo emptyList()
    }

    @Test
    fun `createIntent launches the picked camera activity directly`() {
        val sut = contract(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        val intent = sut.createIntent(context, target(CaptureMediaContract.Target.Kind.VIDEO))

        intent.action shouldBeEqualTo MediaStore.ACTION_VIDEO_CAPTURE
        intent.component shouldBeEqualTo ComponentName("com.camera", "CameraActivity")
        intent.flags shouldBeEqualTo Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        @Suppress("DEPRECATION")
        intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT) shouldBeEqualTo outputUri
    }

    @Test
    fun `createIntent keeps only the destination file of the picked kind`() {
        val sut = contract(
            mode = CaptureMediaContract.Mode.PHOTO_AND_VIDEO,
            pictureFile = File(context.cacheDir, "restored.jpg"),
        )

        sut.createIntent(context, target(CaptureMediaContract.Target.Kind.VIDEO))

        sut.pictureFile.shouldBeNull()
        sut.videoFile.shouldNotBeNull()
    }

    @Test
    fun `parseResult returns the captured file only when the capture succeeded`() {
        val sut = contract(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        sut.createIntent(context, target(CaptureMediaContract.Target.Kind.PHOTO))
        val photo = sut.pictureFile!!
        photo.writeText("captured")

        sut.parseResult(Activity.RESULT_OK, null) shouldBe photo
        sut.parseResult(Activity.RESULT_CANCELED, null).shouldBeNull()
    }

    @Test
    fun `parseResult returns null when the camera app wrote nothing`() {
        val sut = contract(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        sut.createIntent(context, target(CaptureMediaContract.Target.Kind.PHOTO))

        sut.parseResult(Activity.RESULT_OK, null).shouldBeNull()
    }

    private fun contract(mode: CaptureMediaContract.Mode, pictureFile: File? = null) = CaptureMediaContract(
        mode = mode,
        pictureFile = pictureFile,
        uriProvider = mock { on { getUriForFile(any(), any()) } doReturn outputUri },
    )

    private fun target(kind: CaptureMediaContract.Target.Kind) = CaptureMediaContract.Target(
        kind = kind,
        activityInfo = ActivityInfo().apply {
            packageName = "com.camera"
            name = "CameraActivity"
        },
    )

    private fun registerCameraActivity(action: String, name: String) {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.camera"
                this.name = name
            }
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(Intent(action), resolveInfo)
    }
}
