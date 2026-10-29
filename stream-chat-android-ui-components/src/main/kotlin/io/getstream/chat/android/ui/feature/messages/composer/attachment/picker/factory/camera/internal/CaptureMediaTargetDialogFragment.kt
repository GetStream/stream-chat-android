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

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.databinding.StreamUiDialogCaptureMediaTargetBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemCaptureMediaTargetBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.log.taggedLogger
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Lets the user pick the camera app to capture media with, when several are available.
 */
internal class CaptureMediaTargetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: StreamUiDialogCaptureMediaTargetBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StreamUiDialogCaptureMediaTargetBinding.inflate(
            requireContext().streamThemeInflater,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mode = CaptureMediaContract.Mode.valueOf(requireArguments().getString(ARG_MODE)!!)
        val contract = CaptureMediaContract(mode)
        val targets = contract.targets(requireContext())
        binding.titleTextView.text = contract.title(requireContext())
        binding.targetsRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), targets.size.coerceIn(1, MAX_COLUMNS))
        binding.targetsRecyclerView.adapter = TargetAdapter(targets) { target ->
            setFragmentResult(REQUEST_KEY, target.toBundle())
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class TargetAdapter(
        private val targets: List<CaptureMediaContract.Target>,
        private val onTargetClick: (CaptureMediaContract.Target) -> Unit,
    ) : RecyclerView.Adapter<TargetViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TargetViewHolder =
            TargetViewHolder(
                StreamUiItemCaptureMediaTargetBinding.inflate(parent.context.streamThemeInflater, parent, false),
            )

        override fun onBindViewHolder(holder: TargetViewHolder, position: Int) {
            val target = targets[position]
            val packageManager = holder.itemView.context.packageManager
            holder.binding.iconImageView.setImageDrawable(target.activityInfo.loadIcon(packageManager))
            holder.binding.labelTextView.text = target.activityInfo.loadLabel(packageManager)
            holder.binding.subtitleTextView.setText(
                when (target.kind) {
                    CaptureMediaContract.Target.Kind.PHOTO ->
                        UiCommonR.string.stream_ui_message_composer_capture_media_take_photo
                    CaptureMediaContract.Target.Kind.VIDEO ->
                        UiCommonR.string.stream_ui_message_composer_capture_media_video
                },
            )
            holder.itemView.setOnClickListener { onTargetClick(target) }
        }

        override fun getItemCount(): Int = targets.size
    }

    private class TargetViewHolder(val binding: StreamUiItemCaptureMediaTargetBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TAG = "CaptureMediaTargetDialogFragment"
        private const val REQUEST_KEY = "capture_media_target_request_key"
        private const val ARG_MODE = "mode"
        private const val KEY_KIND = "kind"
        private const val KEY_ACTIVITY_INFO = "activity_info"
        private const val MAX_COLUMNS = 4

        private fun CaptureMediaContract.Target.toBundle() = bundleOf(
            KEY_KIND to kind.name,
            KEY_ACTIVITY_INFO to activityInfo,
        )

        private fun Bundle.toTarget(): CaptureMediaContract.Target? {
            val kind = getString(KEY_KIND)?.let(CaptureMediaContract.Target.Kind::valueOf) ?: return null
            val activityInfo = BundleCompat.getParcelable(this, KEY_ACTIVITY_INFO, ActivityInfo::class.java)
                ?: return null
            return CaptureMediaContract.Target(kind, activityInfo)
        }

        /**
         * Lists the camera apps for [mode] and hands the one to launch to [onTargetPicked], asking the user
         * to pick one when there are several.
         *
         * Must be called from [Fragment.onViewCreated], so the pick is still delivered after the host
         * fragment is recreated while the dialog is showing.
         */
        fun register(
            host: Fragment,
            mode: CaptureMediaContract.Mode,
            onTargetPicked: (CaptureMediaContract.Target) -> Unit,
        ): () -> Unit {
            host.childFragmentManager.setFragmentResultListener(REQUEST_KEY, host.viewLifecycleOwner) { _, result ->
                result.toTarget()?.let(onTargetPicked)
            }
            return {
                val targets = CaptureMediaContract(mode).targets(host.requireContext())
                when {
                    targets.isEmpty() -> logger.w { "[pick] No camera app can capture the requested media" }
                    targets.size == 1 -> onTargetPicked(targets.single())
                    host.childFragmentManager.findFragmentByTag(TAG) == null ->
                        CaptureMediaTargetDialogFragment()
                            .apply { arguments = bundleOf(ARG_MODE to mode.name) }
                            .show(host.childFragmentManager, TAG)
                }
            }
        }

        private val logger by taggedLogger("Chat:CaptureMediaTarget")
    }
}
