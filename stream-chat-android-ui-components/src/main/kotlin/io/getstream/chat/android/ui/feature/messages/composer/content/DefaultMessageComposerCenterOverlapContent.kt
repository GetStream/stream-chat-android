/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.transition.Fade
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.extensions.limitTo
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterOverlapContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.displayMetrics
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.log.taggedLogger

private const val TAG = "OverlappingContent"

@InternalStreamChatApi
public class DefaultMessageComposerOverlappingContent : ConstraintLayout, MessageComposerContent {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    public var recordButtonHoldListener: () -> Unit = {}
    public var recordButtonLockListener: () -> Unit = {}
    public var recordButtonCancelListener: () -> Unit = {}
    public var recordButtonReleaseListener: () -> Unit = {}

    public var playbackButtonClickListener: () -> Unit = {}
    public var stopButtonClickListener: () -> Unit = {}
    public var deleteButtonClickListener: () -> Unit = {}
    public var completeButtonClickListener: () -> Unit = {}
    public var sliderDragStartListener: (Float) -> Unit = {}
    public var sliderDragStopListener: (Float) -> Unit = {}

    private val logger by taggedLogger(TAG)

    private fun vibrateDevice(milliseconds: Long) {
        val vibrator = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                ContextCompat.getSystemService(context, Vibrator::class.java)
            }

            else -> {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }

        // Check if the device has vibrator capabilities
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                vibrator.vibrate(milliseconds)
            }
        }
    }

    private val binding: StreamUiMessageComposerDefaultCenterOverlapContentBinding

    private var parentWidth: Int = Int.MAX_VALUE
    private var centerContentHeight: Int = Int.MAX_VALUE

    private var lockPopup: PopupWindow? = null
    private val lockBaseRect = Rect()
    private val lockLastRect = Rect()
    private val lockMoveRect = Rect()

    private var micPopup: PopupWindow? = null
    private val micOrigRect = Rect()
    private val micBaseRect = Rect()
    private val micLastRect = Rect()
    private val micMoveRect = Rect()

    private var holdPopup: PopupWindow? = null
    private var holdStartTime = 0L
    private val holdHandler = Handler(Looper.getMainLooper())

    private val baseTouch = FloatArray(size = 2)

    private val permissionChecker = PermissionChecker()

    private var _state: RecordingState = RecordingState.Idle
        set(value) {
            logger.i { "[setState] state: $value" }
            field = value
        }

    init {
        logger.i { "<init> state: $_state" }
        val inflater = LayoutInflater.from(context)
        binding = StreamUiMessageComposerDefaultCenterOverlapContentBinding.inflate(inflater, this)

        binding.recordingDelete.setOnClickListener {
            deleteButtonClickListener()
        }

        binding.recordingStop.setOnClickListener {
            stopButtonClickListener()
        }
        binding.recordingComplete.setOnClickListener {
            completeButtonClickListener()
        }
        binding.recordingPlayback.setOnClickListener {
            playbackButtonClickListener()
        }
        binding.recordingWaveform.onSliderDragStart = { progress ->
            sliderDragStartListener(progress)
        }
        binding.recordingWaveform.onSliderDragStop = { progress ->
            sliderDragStopListener(progress)
        }
        centerContentHeight = context.getDimension(R.dimen.stream_ui_message_composer_center_content_height)
        parentWidth = displayMetrics().widthPixels
        renderIdle()
    }

    /**
     * The composer context.
     */
    private lateinit var composerContext: MessageComposerContext

    /**
     * The style for [MessageComposerView].
     */
    private lateinit var style: MessageComposerViewStyle

    override fun attachContext(messageComposerContext: MessageComposerContext) {
        composerContext = messageComposerContext
        style = messageComposerContext.style

        binding.recordingSlider.text = style.audioRecordingSlideToCancelText
    }

    override fun renderState(state: MessageComposerState) {
        val recording = state.recording
        logger.i { "[renderState] recordingState: ${recording::class.simpleName}" }
        when (recording) {
            is RecordingState.Hold -> renderHold(recording)
            is RecordingState.Locked -> renderLocked(recording)
            is RecordingState.Overview -> renderOverview(recording)
            is RecordingState.Complete -> renderComplete()
            is RecordingState.Idle -> renderIdle()
        }
        _state = recording
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderIdle()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            logger.i { "[onVisibilityChanged] VISIBLE; state: $_state" }
        } else {
            logger.i { "[onVisibilityChanged] NOT_VISIBLE; state: $_state" }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return _state is RecordingState.Idle || _state is RecordingState.Hold
    }

    private fun resetUI() {
        logger.d { "[resetUI] no args" }
        binding.recordingWaveform.clearData()
        binding.recordingSlider.translationX = 0f
        binding.recordingSlider.alpha = 1f

        micLastRect.set(micBaseRect)
        lockLastRect.set(lockBaseRect)
        lockPopup?.dismiss()
        lockPopup = null
        micPopup?.dismiss()
        micPopup = null
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        logger.d { "[onLayout] w: $width, h: $height" }
    }

    private fun renderIdle() {
        val state = _state
        logger.i { "[renderIdle] state: $state" }
        isVisible = false
        resetUI()
    }

    private fun renderComplete() {
        val state = _state
        logger.i { "[renderComplete] state: $state" }
        resetUI()
    }

    private fun renderHold(state: RecordingState.Hold) {
        logger.d { "[renderHold] no args" }
        holdPopup?.dismiss()
        holdPopup = null

        isVisible = true
        binding.horizontalGuideline.setGuidelinePercent(1f)
        layoutParams.height = centerContentHeight
        binding.recordingSlider.isVisible = true

        binding.recordingPlayback.setImageResource(R.drawable.stream_ui_ic_mic)
        binding.recordingPlayback.setImageColorRes(R.color.stream_ui_accent_red)
        binding.recordingPlayback.isVisible = true
        binding.recordingPlayback.isClickable = false
        binding.recordingPlayback.isFocusable = false
        binding.recordingWaveform.isVisible = false
        binding.recordingWaveform.isSliderVisible = false
        binding.recordingStop.isVisible = false
        binding.recordingDelete.isVisible = false
        binding.recordingComplete.isVisible = false

        binding.recordingTimer.text = formatMillis(state.durationInMs)
    }

    private fun renderLocked(state: RecordingState.Locked) {
        logger.d { "[renderLocked] waveform: ${state.waveform.size}" }

        isVisible = true
        layoutParams.height = centerContentHeight * 2
        binding.horizontalGuideline.setGuidelinePercent(0.5f)
        binding.recordingSlider.isVisible = false

        binding.recordingDelete.isVisible = true
        binding.recordingStop.isVisible = true
        binding.recordingComplete.isVisible = true

        binding.recordingTimer.text = formatMillis(state.durationInMs)
        binding.recordingWaveform.isVisible = true
        binding.recordingWaveform.waveform = state.waveform
        binding.recordingWaveform.isSliderVisible = false

        micPopup?.dismiss()
        micPopup = null

        val lockedIconDrawable = style.audioRecordingLockedIconDrawable.applyTint(
            style.audioRecordingLockedIconDrawableTint
        )
        (lockPopup?.contentView as? ImageView?)?.setImageDrawable(lockedIconDrawable)
        lockPopup?.update(lockBaseRect.left, lockBaseRect.top, NO_CHANGE, lockBaseRect.width())
    }

    private fun renderOverview(state: RecordingState.Overview) {
        logger.d { "[renderOverview] state.isPlaying: ${state.isPlaying}" }

        isVisible = true
        layoutParams.height = centerContentHeight * 2
        binding.horizontalGuideline.setGuidelinePercent(0.5f)

        binding.recordingPlayback.setImageResource(
            when (state.isPlaying) {
                true -> R.drawable.stream_ui_ic_pause
                else -> R.drawable.stream_ui_ic_play
            }
        )
        binding.recordingPlayback.setImageColorRes(R.color.stream_ui_accent_blue)
        binding.recordingPlayback.isClickable = true
        binding.recordingPlayback.isFocusable = true
        binding.recordingPlayback.isVisible = true

        binding.recordingSlider.isVisible = false

        binding.recordingTimer.text = formatMillis(0)
        binding.recordingWaveform.isVisible = true
        binding.recordingWaveform.waveform = state.waveform
        binding.recordingWaveform.waveform = state.waveform
        binding.recordingWaveform.isSliderVisible = true
        binding.recordingWaveform.progress = state.playingProgress

        binding.recordingDelete.isVisible = true
        binding.recordingStop.isVisible = false
        binding.recordingComplete.isVisible = true

        micPopup?.dismiss()
        micPopup = null
        lockPopup?.dismiss()
        lockPopup = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val state = _state
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN && state !is RecordingState.Idle) {
            logger.w { "[onTouchEvent] rejected ACTION_DOWN (state is not Idle): $state" }
            return false
        }
        if (action != MotionEvent.ACTION_DOWN && state !is RecordingState.Hold) {
            logger.w { "[onTouchEvent] rejected ${actionToString(action)} (state is not Hold): $state" }
            return false
        }
        logger.v { "[onTouchEvent] state: $state, event: $event" }
        val x = event.rawX
        val y = event.rawY
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                logger.i { "[onTouchEvent] ACTION_DOWN" }
                if (!permissionChecker.isGrantedAudioRecordPermission(context)) {
                    permissionChecker.checkAudioRecordPermissions(this)
                    return false
                }
                parentWidth = minOf(parentWidth, composerContext.content.asView().width)
                centerContentHeight = context.getDimension(R.dimen.stream_ui_message_composer_center_content_height)
                val recordAudioButton =
                    composerContext.content.findRecordAudioButton()
                        ?: composerContext.content.trailing?.asView()?.findViewById(R.id.recordAudioButton)
                        ?: error("recordAudioButton not found")
                recordAudioButton.getRectInWindow(micOrigRect)

                logger.w { "[onTouchEvent] centerContentHeight: $centerContentHeight, ${displayMetrics().density}, ${displayMetrics().widthPixels}" }
                baseTouch[0] = x
                baseTouch[1] = y
                holdStartTime = SystemClock.elapsedRealtime()

                showMicPopup()
                showLockPopup()
                vibrateDevice(VIBRATE_MS)
                recordButtonHoldListener()
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = (x - baseTouch[0]).toInt()
                val deltaY = (y - baseTouch[1]).toInt()

                if (micBaseRect.width() == 0 || lockBaseRect.width() == 0) {
                    logger.v { "[onTouchEvent] ACTION_MOVE rejected (no popups 1)" }
                    return false
                }
                if (micPopup == null || lockPopup == null) {
                    logger.v { "[onTouchEvent] ACTION_MOVE rejected (no popups 2)" }
                    return false
                }
                logger.i { "[onTouchEvent] ACTION_MOVE: offsetX: $deltaX, offsetY: $deltaY" }

                val micX = micBaseRect.left + deltaX
                val micY = micBaseRect.top + deltaY
                micLastRect.left = micX.limitTo(micMoveRect.left, micBaseRect.left)
                micLastRect.top = micY.limitTo(micMoveRect.top, micBaseRect.top)
                micPopup?.update(micLastRect.left, micLastRect.top, -1, -1)

                val lockY = lockBaseRect.top + deltaY
                lockLastRect.top = lockY.limitTo(lockMoveRect.top, lockBaseRect.top)
                lockPopup?.update(lockLastRect.left, lockLastRect.top, -1, -1)

                val progress =
                    (micBaseRect.left - micLastRect.left).toFloat() / (micBaseRect.left - micMoveRect.left).toFloat()
                logger.w { "[onMove] progress: $progress, diff: ${micLastRect.left - micBaseRect.left}" }
                binding.recordingSlider.translationX = micLastRect.left - micBaseRect.left.toFloat()
                binding.recordingSlider.alpha = 1 - progress * 1.5f

                if (micLastRect.left == micMoveRect.left) {
                    logger.w { "[onMove] cancelled; micLastRect: $micLastRect, micMoveRect: $micMoveRect" }
                    renderIdle()
                    recordButtonCancelListener()
                    return false
                }

                if (micLastRect.top == micMoveRect.top) {
                    logger.w { "[onMove] locked" }
                    recordButtonLockListener()
                    return false
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                logger.d { "[onTouchEvent] ACTION_CANCEL" }
                recordButtonCancelListener()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val duration = SystemClock.elapsedRealtime() - holdStartTime
                logger.d { "[onTouchEvent] ACTION_UP ($duration)" }
                vibrateDevice(VIBRATE_MS)
                if (duration > HOLD_TIMEOUT_MS) {
                    recordButtonReleaseListener()
                } else {
                    showHoldPopup()
                    recordButtonCancelListener()
                }
                return true
            }
        }
        return true
    }

    private fun showMicPopup() {
        logger.d { "[showMicPopup] micOrigRect: $micOrigRect" }
        val micContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_mic, this, false
        )
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        micContent.measure(widthMeasureSpec, heightMeasureSpec)
        val micW = maxOf(micContent.measuredWidth, micContent.layoutParams.width)
        val micH = maxOf(micContent.measuredHeight, micContent.layoutParams.height)

        micPopup?.dismiss()
        micPopup = PopupWindow(context).apply {
            setBackgroundDrawable(null)
            isClippingEnabled = false

            contentView = micContent
            width = micW
            height = micH

            micBaseRect.set(0, 0, micW, micH)
            val deltaX = micOrigRect.centerX() - micBaseRect.centerX()
            val deltaY = micOrigRect.centerY() - micBaseRect.centerY()
            micBaseRect.offset(deltaX, deltaY)
            micLastRect.set(micBaseRect)
            micMoveRect.apply {
                set(micBaseRect)
                left -= (parentWidth / 3)
                top -= centerContentHeight * 2
            }
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, micBaseRect.left, micBaseRect.top)
        }
    }

    private fun showLockPopup() {
        logger.d { "[showLockPopup] micBaseRect: $micBaseRect" }
        val lockContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_lock, this, false
        ).also { imageView ->
            (imageView as ImageView).apply {
                val iconDrawable = style.audioRecordingLockIconDrawable.applyTint(
                    style.audioRecordingLockIconDrawableTint
                )
                setImageDrawable(iconDrawable)
            }
        }
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        lockContent.measure(widthMeasureSpec, heightMeasureSpec)
        val lockW = maxOf(lockContent.measuredWidth, lockContent.layoutParams.width)
        val lockH = maxOf(lockContent.measuredHeight, lockContent.layoutParams.height)
        lockPopup?.dismiss()
        lockPopup = PopupWindow(context).apply {
            setBackgroundDrawable(null)
            isClippingEnabled = true

            contentView = lockContent
            width = lockW
            height = lockH

            lockBaseRect.set(0, 0, lockW, lockH)
            val deltaX = micBaseRect.centerX() - lockBaseRect.centerX()
            val spacerY = 16.dpToPx()
            val deltaY = micBaseRect.top - lockBaseRect.height() - spacerY
            lockBaseRect.offset(deltaX, deltaY)

            lockLastRect.set(lockBaseRect)
            lockMoveRect.apply {
                set(lockBaseRect)
                top -= centerContentHeight * 2
            }
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, lockBaseRect.left, lockBaseRect.top)
        }
    }

    private fun showHoldPopup() {
        val holdContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_hold, this, false
        ).also {
            it.findViewById<TextView>(R.id.holdToRecordText).apply {
                text = style.audioRecordingHoldToRecordText
                setTextColor(style.audioRecordingHoldToRecordTextColor)
                background = style.audioRecordingHoldToRecordBackgroundDrawable.applyTint(
                    style.audioRecordingHoldToRecordBackgroundDrawableTint
                )
            }
        }
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        holdContent.measure(widthMeasureSpec, heightMeasureSpec)
        val popupH = maxOf(holdContent.measuredHeight, holdContent.layoutParams.height)

        logger.d { "[showHoldPopup] holdContent.h: ${holdContent.measuredHeight} - ${holdContent.layoutParams.height}" }
        holdPopup?.dismiss()
        holdPopup = PopupWindow(context).apply {
            setBackgroundDrawable(null)
            isClippingEnabled = true

            contentView = holdContent
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = popupH

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                enterTransition = Fade(Fade.IN).apply {
                    duration = ANIM_DURATION_MS
                }
                exitTransition = Fade(Fade.OUT).apply {
                    duration = ANIM_DURATION_MS
                }
            }
            val xy = IntArray(2)
            binding.root.getLocationInWindow(xy)
            val top = xy[1] - popupH
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, 0, top)
        }

        holdHandler.removeCallbacksAndMessages(null)
        holdHandler.postDelayed({
            logger.v { "[showHoldPopup] delayed cancellation" }
            holdPopup?.dismiss()
            holdPopup = null
        }, HOLD_TIMEOUT_MS)
    }

    internal companion object {
        private const val HOLD_TIMEOUT_MS = 1000L
        private const val ANIM_DURATION_MS = 100L
        private const val VIBRATE_MS = 100L
        private const val NO_CHANGE = -1
    }
}

private fun View.getRectInWindow(out: Rect) {
    val xy = IntArray(2)
    getLocationInWindow(xy)
    out.apply {
        left = xy[0]
        top = xy[1]
        right = xy[0] + width
        bottom = xy[1] + height
    }
}

private val ViewParent.id: Int
    get() = (this as? View)?.id ?: ConstraintLayout.NO_ID

private fun formatMillis(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun ImageView.setImageColor(@ColorInt color: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
}

private fun ImageView.setImageColorRes(@ColorRes colorResId: Int) {
    val color = ContextCompat.getColor(context, colorResId)
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color))
}

private fun actionToString(action: Int): String {
    return when (action) {
        MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
        MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
        MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
        MotionEvent.ACTION_UP -> "ACTION_UP"
        else -> "ACTION_UNKNOWN"
    }
}
