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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
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
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterOverlapContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.displayMetrics
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import io.getstream.chat.android.ui.utils.extensions.setStartDrawable
import io.getstream.log.taggedLogger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val TAG = "OverlappingContent"

/**
 * Represents the content which overlaps [MessageComposerCenterContent] in [MessageComposerView].
 */
public interface MessageComposerOverlappingContent : MessageComposerContent {
    /**
     * Hold listener for the record button.
     */
    public var recordButtonHoldListener: (() -> Unit)?

    /**
     * Lock listener for the record button.
     */
    public var recordButtonLockListener: (() -> Unit)?

    /**
     * Cancel listener for the record button.
     */
    public var recordButtonCancelListener: (() -> Unit)?

    /**
     * Release listener for the record button.
     */
    public var recordButtonReleaseListener: (() -> Unit)?

    /**
     * Click listener for the playback button.
     */
    public var playbackButtonClickListener: (() -> Unit)?

    /**
     * Click listener for the stop button.
     */
    public var stopButtonClickListener: (() -> Unit)?

    /**
     * Click listener for the delete button.
     */
    public var deleteButtonClickListener: (() -> Unit)?

    /**
     * Click listener for the complete button.
     */
    public var completeButtonClickListener: (() -> Unit)?

    /**
     * Drag start listener for the slider.
     */
    public var sliderDragStartListener: ((Float) -> Unit)?

    /**
     * Drag stop listener for the slider.
     */
    public var sliderDragStopListener: ((Float) -> Unit)?
}

/**
 * Represents the content which overlaps [MessageComposerCenterContent] in [MessageComposerView].
 */
@Suppress("TooManyFunctions")
public open class DefaultMessageComposerOverlappingContent : ConstraintLayout, MessageComposerOverlappingContent {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes,
    )

    public override var recordButtonHoldListener: (() -> Unit)? = null
    public override var recordButtonLockListener: (() -> Unit)? = null
    public override var recordButtonCancelListener: (() -> Unit)? = null
    public override var recordButtonReleaseListener: (() -> Unit)? = null

    public override var playbackButtonClickListener: (() -> Unit)? = null
    public override var stopButtonClickListener: (() -> Unit)? = null
    public override var deleteButtonClickListener: (() -> Unit)? = null
    public override var completeButtonClickListener: (() -> Unit)? = null
    public override var sliderDragStartListener: ((Float) -> Unit)? = null
    public override var sliderDragStopListener: ((Float) -> Unit)? = null

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
            deleteButtonClickListener?.invoke()
        }

        binding.recordingStop.setOnClickListener {
            stopButtonClickListener?.invoke()
        }
        binding.recordingComplete.setOnClickListener {
            completeButtonClickListener?.invoke()
        }
        binding.recordingPlayback.setOnClickListener {
            playbackButtonClickListener?.invoke()
        }
        binding.recordingWaveform.onSliderDragStart = { progress ->
            sliderDragStartListener?.invoke(progress)
        }
        binding.recordingWaveform.onSliderDragStop = { progress ->
            sliderDragStopListener?.invoke(progress)
        }
        centerContentHeight = context.getDimension(R.dimen.stream_ui_message_composer_center_content_height)
        parentWidth = displayMetrics().widthPixels
        renderIdle()
    }

    /**
     * The composer context.
     */
    protected lateinit var composerContext: MessageComposerContext

    /**
     * The style for [MessageComposerView].
     */
    protected lateinit var style: MessageComposerViewStyle

    override fun attachContext(messageComposerContext: MessageComposerContext) {
        composerContext = messageComposerContext
        style = messageComposerContext.style

        binding.recordingSlider.text = style.audioRecordingSlideToCancelText
        binding.recordingSlider.setTextStyle(style.audioRecordingSlideToCancelTextStyle)
        binding.recordingSlider.setStartDrawable(
            style.audioRecordingSlideToCancelStartDrawable
                .apply { DrawableCompat.setLayoutDirection(this, resources.configuration.layoutDirection) }
                .applyTint(style.audioRecordingSlideToCancelStartDrawableTint),
        )
        style.audioRecordingWaveformColor?.also {
            binding.recordingWaveform.setWaveformColor(it)
        }
    }

    override fun renderState(state: MessageComposerState) {
        val recording = state.recording
        logger.i { "[renderState] measuredHeight: $measuredHeight, recordingState: ${recording::class.simpleName}" }
        when (recording) {
            is RecordingState.Hold -> renderHold(recording)
            is RecordingState.Locked -> renderLocked(recording)
            is RecordingState.Overview -> renderOverview(recording)
            is RecordingState.Complete -> renderComplete()
            is RecordingState.Idle -> renderIdle()
        }
        _state = recording
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        logger.i { "[onAttachedToWindow] newConfig: $newConfig" }
        super.onConfigurationChanged(newConfig)
    }

    override fun onAttachedToWindow() {
        logger.i { "[onAttachedToWindow] no args" }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        logger.i { "[onDetachedFromWindow] no args" }
        super.onDetachedFromWindow()
        renderIdle()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        logger.w { "[onRestoreInstanceState] state: $state" }
        super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable? {
        logger.w { "[onSaveInstanceState] no args" }
        return super.onSaveInstanceState()
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
        logger.d { "[renderLocked] waveform: ${state.waveform.size}, windowToken: $windowToken" }

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

        if (_state !is RecordingState.Locked || lockPopup == null) {
            showLockedPopup()
        }

        // TODO delete this when we switch to usage of `showAsDropDown` inside of `showLockPopup`
        // val lockedIconDrawable = style.audioRecordingLockedIconDrawable.applyTint(
        //     style.audioRecordingLockedIconDrawableTint
        // )
        // (lockPopup?.contentView as? ImageView?)?.setImageDrawable(lockedIconDrawable)
        // lockPopup?.update(lockBaseRect.left, lockBaseRect.top, NO_CHANGE, lockBaseRect.width())
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
            },
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
    @Suppress("ReturnCount", "LongMethod")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val state = _state
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN && state !is RecordingState.Idle) {
            logger.w { "[onTouchEvent] rejected ACTION_DOWN (state is not Idle): $state" }
            return false
        }
        // For other actions, require Hold state OR allow gesture-end actions during transition window
        val isGestureEndAction = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
        val isInRecordingTransition = (micPopup != null || lockPopup != null) && _state is RecordingState.Idle
        val shouldAllowTransitionWindow = isGestureEndAction && isInRecordingTransition
        if (action != MotionEvent.ACTION_DOWN && state !is RecordingState.Hold && !shouldAllowTransitionWindow) {
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

                logger.w {
                    "[onTouchEvent] centerContentHeight: $centerContentHeight, ${displayMetrics().density}, " +
                        "${displayMetrics().widthPixels}"
                }
                baseTouch[0] = x
                baseTouch[1] = y
                holdStartTime = SystemClock.elapsedRealtime()

                showMicPopup()
                showLockPopup()
                vibrateDevice(VIBRATE_MS)
                recordButtonHoldListener?.invoke()
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
                micLastRect.left = micX.coerceIn(
                    min(micBaseRect.left, micMoveRect.left),
                    max(micBaseRect.left, micMoveRect.left),
                )
                micLastRect.top = micY.coerceIn(micMoveRect.top, micBaseRect.top)
                micPopup?.update(micLastRect.left, micLastRect.top, -1, -1)

                val lockY = lockBaseRect.top + deltaY
                lockLastRect.top = lockY.coerceIn(lockMoveRect.top, lockBaseRect.top)
                lockPopup?.update(lockLastRect.left, lockLastRect.top, -1, -1)

                val totalDistance = abs(micBaseRect.left - micMoveRect.left).toFloat()
                val progress = abs(micBaseRect.left - micLastRect.left).toFloat() / totalDistance

                logger.w { "[onMove] progress: $progress, diff: ${micLastRect.left - micBaseRect.left}" }
                binding.recordingSlider.translationX = micLastRect.left - micBaseRect.left.toFloat()
                binding.recordingSlider.alpha = 1 - progress * 1.5f

                if (micLastRect.left == micMoveRect.left) {
                    logger.w { "[onMove] cancelled; micLastRect: $micLastRect, micMoveRect: $micMoveRect" }
                    renderIdle()
                    recordButtonCancelListener?.invoke()
                    return false
                }

                if (micLastRect.top == micMoveRect.top) {
                    logger.w { "[onMove] locked" }
                    recordButtonLockListener?.invoke()
                    return false
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                logger.d { "[onTouchEvent] ACTION_CANCEL" }
                if (isInRecordingTransition) {
                    resetUI()
                }
                recordButtonCancelListener?.invoke()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val duration = SystemClock.elapsedRealtime() - holdStartTime
                logger.d { "[onTouchEvent] ACTION_UP ($duration)" }
                vibrateDevice(VIBRATE_MS)
                if (duration > HOLD_TIMEOUT_MS) {
                    recordButtonReleaseListener?.invoke()
                } else {
                    if (isInRecordingTransition) {
                        resetUI()
                    }
                    showHoldPopup()
                    recordButtonCancelListener?.invoke()
                }
                return true
            }
        }
        return true
    }

    private fun showMicPopup() {
        if (windowToken == null) {
            logger.w { "[showMicPopup] rejected (windowToken is null)" }
            return
        }
        logger.d { "[showMicPopup] micOrigRect: $micOrigRect" }
        val micContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_mic,
            this,
            false,
        ).also { imageView ->
            (imageView as ImageView).apply {
                val iconDrawable = style.audioRecordingFloatingButtonIconDrawable.applyTint(
                    style.audioRecordingFloatingButtonIconDrawableTint,
                )
                setImageDrawable(iconDrawable)
                val bgDrawable = style.audioRecordingFloatingButtonBackgroundDrawable.applyTint(
                    style.audioRecordingFloatingButtonBackgroundDrawableTint,
                )
                background = bgDrawable
            }
        }
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
                if (context.isRtlLayout) {
                    left += (parentWidth / 3)
                } else {
                    left -= (parentWidth / 3)
                }
                top -= centerContentHeight * 2
            }
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, micBaseRect.left, micBaseRect.top)
        }
    }

    private fun showLockPopup() {
        if (windowToken == null) {
            logger.w { "[showLockPopup] rejected (windowToken is null)" }
            return
        }
        logger.d { "[showLockPopup] micBaseRect: $micBaseRect" }
        val lockContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_lock,
            this,
            false,
        ).also { imageView ->
            (imageView as ImageView).apply {
                val iconDrawable = style.audioRecordingFloatingLockIconDrawable.applyTint(
                    style.audioRecordingFloatingLockIconDrawableTint,
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
            // TODO make it work using showAsDropDown, cause we won't have a proper way to restore it's position
            // showAsDropDown(binding.root, 0, 200, Gravity.BOTTOM or Gravity.END)
        }
    }

    private fun showLockedPopup() {
        if (windowToken == null) {
            logger.w { "[showLockedPopup] rejected (windowToken is null)" }
            return
        }
        logger.d { "[showLockedPopup] micBaseRect: $micBaseRect" }
        val lockContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_locked,
            this,
            false,
        ).also { imageView ->
            (imageView as ImageView).apply {
                val iconDrawable = style.audioRecordingFloatingLockedIconDrawable.applyTint(
                    style.audioRecordingFloatingLockedIconDrawableTint,
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

            val spacerY = 16.dpToPx()
            showAsDropDown(binding.root, 0, -spacerY, Gravity.BOTTOM or Gravity.END)
        }
    }

    private fun showHoldPopup() {
        val holdContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_hold,
            this,
            false,
        ).also {
            it.findViewById<TextView>(R.id.holdToRecordText).apply {
                text = style.audioRecordingHoldToRecordText
                setTextStyle(style.audioRecordingHoldToRecordTextStyle)
                background = style.audioRecordingHoldToRecordBackgroundDrawable.applyTint(
                    style.audioRecordingHoldToRecordBackgroundDrawableTint,
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
