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

package io.getstream.chat.android.ui.feature.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.ui.feature.gallery.internal.AttachmentGalleryRepository
import io.getstream.chat.android.ui.navigation.destinations.ChatDestination

public class AttachmentGalleryDestination(
    context: Context,
    private val attachmentReplyOptionHandler: AttachmentGalleryActivity.AttachmentReplyOptionHandler,
    private val attachmentShowInChatOptionHandler: AttachmentGalleryActivity.AttachmentShowInChatOptionHandler,
    private val attachmentDownloadOptionHandler: AttachmentGalleryActivity.AttachmentDownloadOptionHandler,
    private val attachmentDeleteOptionClickHandler: AttachmentGalleryActivity.AttachmentDeleteOptionHandler,
) : ChatDestination(context) {
    private var launcher: ActivityResultLauncher<AttachmentGalleryResultContract.Input>? = null

    private lateinit var attachmentGalleryItems: List<AttachmentGalleryItem>
    private var attachmentIndex: Int = 0

    public fun setData(attachmentGalleryItems: List<AttachmentGalleryItem>, attachmentIndex: Int) {
        this.attachmentGalleryItems = attachmentGalleryItems
        this.attachmentIndex = attachmentIndex
    }

    override fun navigate() {
        if (attachmentGalleryItems.isEmpty()) {
            Toast.makeText(context, "Invalid image(s)!", Toast.LENGTH_SHORT).show()
            return
        }

        AttachmentGalleryRepository.setAttachmentGalleryItems(attachmentGalleryItems)
        launcher?.launch(AttachmentGalleryResultContract.Input(attachmentIndex))
    }

    public fun register(activityResultRegistry: ActivityResultRegistry) {
        launcher = activityResultRegistry.register(
            "attachment_gallery_launcher#${hashCode()}",
            AttachmentGalleryResultContract(),
        ) { result ->
            when (result) {
                is AttachmentGalleryActivity.AttachmentOptionResult.Reply -> {
                    attachmentReplyOptionHandler.onClick(result.result)
                }
                is AttachmentGalleryActivity.AttachmentOptionResult.ShowInChat -> {
                    attachmentShowInChatOptionHandler.onClick(result.result)
                }
                is AttachmentGalleryActivity.AttachmentOptionResult.Delete -> {
                    attachmentDeleteOptionClickHandler.onClick(result.result)
                }
                is AttachmentGalleryActivity.AttachmentOptionResult.Download -> {
                    attachmentDownloadOptionHandler.onClick(result.result)
                }
                null -> Unit // No result from Gallery, do nothing
            }
        }
    }

    public fun unregister() {
        launcher?.unregister()
        launcher = null
    }
}

private class AttachmentGalleryResultContract :
    ActivityResultContract<AttachmentGalleryResultContract.Input, AttachmentGalleryActivity.AttachmentOptionResult?>() {

    class Input(val attachmentIndex: Int)

    override fun createIntent(context: Context, input: Input): Intent {
        return AttachmentGalleryActivity.createIntent(context, input.attachmentIndex)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AttachmentGalleryActivity.AttachmentOptionResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getParcelableExtra(AttachmentGalleryActivity.EXTRA_ATTACHMENT_OPTION_RESULT)
    }
}
