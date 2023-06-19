package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterOverlapContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.log.taggedLogger

private const val TAG = "OverlappingContent"

@OptIn(InternalStreamChatApi::class)
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

    private val logger by taggedLogger(TAG)

    private val binding: StreamUiMessageComposerDefaultCenterOverlapContentBinding

    private var parentHeight: Int = 0
    private var parentWidth: Int = 0
    private var centerContentHeight: Int = 0

    private var lockPopup: PopupWindow? = null
    private val lockBaseRect = Rect()
    private val lockLastRect = Rect()
    private val lockMoveRect = Rect()

    private var micPopup: PopupWindow? = null
    private val micOrigRect = Rect()
    private val micBaseRect = Rect()
    private val micLastRect = Rect()
    private val micMoveRect = Rect()

    private val baseTouch = FloatArray(size = 2)

    private var micW: Int = 64.dpToPx()
    private var micH: Int = 64.dpToPx()

    private val permissionChecker = PermissionChecker()

    private var _state: RecordingState = RecordingState.Idle
        set(value) {
            logger.i { "[setState] old: $field, new: $value" }
            field = value
        }

    init {
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
    }

    private lateinit var composerContext: MessageComposerContext

    override fun attachContext(messageComposerContext: MessageComposerContext) {
        composerContext = messageComposerContext
    }

    override fun renderState(state: MessageComposerState) {
        binding.root.isVisible = state.recording != RecordingState.Idle
        val recording = state.recording
        if (recording is RecordingState.Hold) {
            renderHold(recording)
        } else if (recording is RecordingState.Locked) {
            renderLocked(recording)
        } else if (recording is RecordingState.Overview) {
            renderOverview(recording)
        } else if (recording is RecordingState.Complete) {
            renderComplete()
        } else if (recording is RecordingState.Idle) {
            renderIdle()
        }
        _state = recording
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderIdle()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return _state is RecordingState.Idle || _state is RecordingState.Hold
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            logger.i { "[onVisibilityChanged] VISIBLE" }
        } else {
            logger.i { "[onVisibilityChanged] NOT_VISIBLE" }
            resetUI()
        }
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
        logger.i { "[onLayout] w: $width, h: $height" }
    }

    private fun renderIdle() {
        val state = _state
        if (state is RecordingState.Idle) {
            logger.w { "[renderIdle] rejected (state is Idle)" }
            return
        }
        logger.e { "[renderIdle] no args" }
        resetUI()
    }

    private fun renderComplete() {
        val state = _state
        if (state is RecordingState.Complete) {
            logger.w { "[renderComplete] rejected (state is Complete)" }
            return
        }
        logger.e { "[renderComplete] no args" }
        resetUI()
    }

    private fun renderHold(state: RecordingState.Hold) {
        logger.d { "[renderHold] no args" }

        binding.horizontalGuideline.setGuidelinePercent(1f)
        layoutParams.height = centerContentHeight
        binding.recordingSlider.isVisible = true

        binding.recordingPlayback.setImageResource(R.drawable.stream_ui_ic_mic)
        binding.recordingPlayback.setImageColorRes(R.color.stream_ui_accent_red)
        binding.recordingPlayback.isVisible = true
        binding.recordingPlayback.isClickable = false
        binding.recordingPlayback.isFocusable = false
        binding.recordingWaveform.isVisible = false
        binding.recordingStop.isVisible = false
        binding.recordingDelete.isVisible = false
        binding.recordingComplete.isVisible = false

        binding.recordingTimer.text = formatMillis(state.duration)
    }

    private fun renderLocked(state: RecordingState.Locked) {
        logger.d { "[renderLocked] waveform: ${state.waveform.size}" }

        layoutParams.height = parentHeight * 2
        binding.horizontalGuideline.setGuidelinePercent(0.5f)
        binding.recordingSlider.isVisible = false


        binding.recordingDelete.isVisible = true
        binding.recordingStop.isVisible = true
        binding.recordingComplete.isVisible = true

        binding.recordingTimer.text = formatMillis(state.duration)
        binding.recordingWaveform.isVisible = true
        binding.recordingWaveform.waveform = state.waveform

        micPopup?.dismiss()
        micPopup = null
        (lockPopup?.contentView as? ImageView?)?.setImageResource(R.drawable.stream_ui_ic_mic_locked_light)

        lockPopup?.update(lockBaseRect.left, lockBaseRect.top - 16.dpToPx(), -1, 64.dpToPx())
    }

    private fun renderOverview(state: RecordingState.Overview) {
        logger.d { "[renderOverview] state.isPlaying: ${state.isPlaying}" }

        layoutParams.height = parentHeight * 2
        binding.horizontalGuideline.setGuidelinePercent(0.5f)

        binding.recordingPlayback.setImageResource(when (state.isPlaying) {
            true -> R.drawable.stream_ui_ic_pause
            else -> R.drawable.stream_ui_ic_play
        })
        binding.recordingPlayback.setImageColorRes(R.color.stream_ui_accent_blue)
        binding.recordingPlayback.isClickable = true
        binding.recordingPlayback.isFocusable = true
        binding.recordingPlayback.isVisible = true

        binding.recordingSlider.isVisible = false

        binding.recordingTimer.text = formatMillis(0L)
        binding.recordingWaveform.isVisible = true
        binding.recordingWaveform.waveform = state.waveform

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
                parentWidth = composerContext.content.asView().width
                parentHeight = composerContext.content.asView().height
                centerContentHeight =
                    composerContext.content.center?.asView()?.height ?: error("no center content found")
                val recordAudioButton =
                    composerContext.content.findRecordAudioButton()
                        ?: composerContext.content.trailing?.asView()?.findViewById(R.id.recordAudioButton)
                        ?: error("recordAudioButton not found")
                recordAudioButton.getRectInWindow(micOrigRect)
                showMicPopup()
                showLockPopup()
                recordButtonHoldListener()

                baseTouch[0] = x
                baseTouch[1] = y
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
                logger.d { "[onTouchEvent] ACTION_UP" }
                recordButtonReleaseListener()
                return true
            }
        }
        return true
    }

    private fun showMicPopup() {
        logger.d { "[showMicPopup] orig: $micOrigRect" }
        val micW = micW
        val micH = micH
        val micContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_mic, this, false
        )
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
        logger.d { "[showLockPopup] micOrigRect: $micOrigRect" }
        val lockW = 52.dpToPx()
        val lockH = 92.dpToPx()
        val lockContent = LayoutInflater.from(context).inflate(
            R.layout.stream_ui_message_composer_default_center_overlap_floating_lock, this, false
        )
        lockPopup?.dismiss()
        lockPopup = PopupWindow(context).apply {
            setBackgroundDrawable(null)
            isClippingEnabled = true

            contentView = lockContent
            width = lockW
            height = lockH

            lockBaseRect.set(0, 0, lockW, lockH)
            val deltaX = micBaseRect.centerX() - lockBaseRect.centerX()
            val deltaY = micBaseRect.top - lockBaseRect.bottom
            lockBaseRect.offset(deltaX, deltaY)

            lockLastRect.set(lockBaseRect)
            lockMoveRect.apply {
                set(lockBaseRect)
                top -= centerContentHeight * 2
            }
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, lockBaseRect.left, lockBaseRect.top)
        }
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

private fun Int.limitTo(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

private val ViewParent.id: Int
    get() = (this as? View)?.id ?: ConstraintLayout.NO_ID

private fun formatMillis(milliseconds: Long): String {
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