/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.gallery.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentGalleryImageBinding
import io.getstream.chat.android.ui.utils.load

internal class AttachmentGalleryImagePageFragment : Fragment() {

    private var _binding: StreamUiItemAttachmentGalleryImageBinding? = null
    private val binding get() = _binding!!

    private val imageUrl: String? by lazy {
        requireArguments().getString(ARG_IMAGE_URL)
    }

    private var imageClickListener: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = StreamUiItemAttachmentGalleryImageBinding.inflate(inflater)
        .apply { _binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.photoView) {
            load(
                data = imageUrl,
                onStart = {
                    binding.placeHolderImageView.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                },
                onComplete = {
                    binding.placeHolderImageView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                },
            )

            setOnClickListener {
                imageClickListener()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun create(attachment: Attachment, imageClickListener: () -> Unit = {}): Fragment = AttachmentGalleryImagePageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_IMAGE_URL, attachment.imagePreviewUrl)
            }
            this.imageClickListener = imageClickListener
        }
    }
}
