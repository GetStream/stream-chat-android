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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.camera.internal

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class CaptureMediaTargetDialogFragmentTest {

    private val activity = Robolectric.buildActivity(FragmentActivity::class.java)
        .also { it.get().setTheme(R.style.StreamUiTheme) }
        .setup()
        .get()

    private val picked = mutableListOf<CaptureMediaContract.Target>()
    private var cancellations = 0

    @Test
    fun `pick cancels when no camera app is installed`() {
        val pick = registerHost(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        pick()

        cancellations shouldBeEqualTo 1
        picked.shouldBeEmpty()
    }

    @Test
    fun `pick launches the only camera app without asking`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        val pick = registerHost(CaptureMediaContract.Mode.PHOTO)

        pick()

        picked.map { it.activityInfo.name } shouldBeEqualTo listOf("PhotoActivity")
        host.dialog().shouldBeNull()
    }

    @Test
    fun `pick asks the user when several camera apps are available`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        val pick = registerHost(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)

        pick()
        idle()

        host.dialog().shouldNotBeNull()
        picked.shouldBeEmpty()
        cancellations shouldBeEqualTo 0
    }

    @Test
    fun `dismissing the dialog cancels the capture`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        val pick = registerHost(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        pick()
        idle()

        host.dialog()!!.requireDialog().cancel()
        idle()

        cancellations shouldBeEqualTo 1
        picked.shouldBeEmpty()
    }

    @Test
    fun `tapping a camera app picks it`() {
        registerCameraActivity(MediaStore.ACTION_IMAGE_CAPTURE, "PhotoActivity")
        registerCameraActivity(MediaStore.ACTION_VIDEO_CAPTURE, "VideoActivity")
        val pick = registerHost(CaptureMediaContract.Mode.PHOTO_AND_VIDEO)
        pick()
        idle()

        val targets = host.dialog()!!.requireView()
            .findViewById<RecyclerView>(R.id.targetsRecyclerView)
        targets.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        targets.layout(0, 0, targets.measuredWidth, targets.measuredHeight)
        targets.getChildAt(1).performClick()
        idle()

        picked.map { it.kind to it.activityInfo.name } shouldBeEqualTo listOf(
            CaptureMediaContract.Target.Kind.VIDEO to "VideoActivity",
        )
        cancellations shouldBeEqualTo 0
    }

    private lateinit var host: HostFragment

    private fun registerHost(mode: CaptureMediaContract.Mode): () -> Unit {
        host = HostFragment()
        activity.supportFragmentManager.beginTransaction().add(android.R.id.content, host).commitNow()
        return CaptureMediaTargetDialogFragment.register(
            host = host,
            mode = mode,
            onTargetPicked = { picked += it },
            onCancelled = { cancellations++ },
        )
    }

    private fun HostFragment.dialog(): CaptureMediaTargetDialogFragment? =
        childFragmentManager.fragments.filterIsInstance<CaptureMediaTargetDialogFragment>().firstOrNull()

    private fun registerCameraActivity(action: String, name: String) {
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.camera"
                this.name = name
            }
        }
        shadowOf(activity.packageManager).addResolveInfoForIntent(Intent(action), resolveInfo)
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    internal class HostFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            FrameLayout(requireContext())
    }
}
