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

package io.getstream.chat.android.ui.feature.gallery.overview.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogMediaAttachmentBinding
import io.getstream.chat.android.ui.feature.gallery.internal.AttachmentGalleryViewModel

internal class MediaAttachmentDialogFragment : BottomSheetDialogFragment() {
    private var _binding: StreamUiDialogMediaAttachmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttachmentGalleryViewModel by viewModels()
    private var mediaClickListener: (Int) -> Unit = {}

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = StreamUiDialogMediaAttachmentBinding.inflate(inflater, container, false)
        .apply { _binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            closeButton.setOnClickListener {
                dismiss()
            }
            mediaAttachmentGridView.setMediaClickListener {
                mediaClickListener.invoke(it)
            }
            viewModel.attachmentGalleryItemsLiveData.observe(viewLifecycleOwner, mediaAttachmentGridView::setAttachments)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun setMediaClickListener(listener: (Int) -> Unit) {
        mediaClickListener = listener
    }

    internal companion object {
        fun newInstance(): MediaAttachmentDialogFragment = MediaAttachmentDialogFragment()
    }
}
