package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.MediaRecorderState
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultCenterOverlapContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.log.taggedLogger
import java.util.Date

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

    private val logger by taggedLogger(TAG)

    private var parentHeight: Int = 0
    private var parentWidth: Int = 0
    private var centerContentHeight: Int = 0

    private lateinit var binding: StreamUiMessageComposerDefaultCenterOverlapContentBinding

    public var onStateChangeListener: ((state: RecordingState) -> Unit)? = null

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
    private val mediaRecorder = DefaultStreamMediaRecorder()

    private var state: RecordingState = RecordingState.Hold
        set(value) {
            Log.e(TAG, "[setState] value: $value")
            field = value
        }

    init {
        val inflater = LayoutInflater.from(context)
        binding = StreamUiMessageComposerDefaultCenterOverlapContentBinding.inflate(inflater, this)
    }

    private lateinit var composerContext: MessageComposerContext

    override fun attachContext(messageComposerContext: MessageComposerContext) {
        composerContext = messageComposerContext
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun renderState(state: MessageComposerState) {
        binding.root.isVisible = state.recording != RecordingState.Idle
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        resetUI()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return state == RecordingState.Hold
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            Log.e(TAG, "[onVisibilityChanged] VISIBLE")
        } else {
            Log.e(TAG, "[onVisibilityChanged] NOT_VISIBLE")
            resetUI()
        }
    }

    private fun resetUI() {
        Log.e(TAG, "[resetUI] no args")

        binding.recordingSlider.translationX = 0f
        binding.recordingSlider.alpha = 1f
        // TODO binding.recordingTimer.stop()

        micLastRect.set(micBaseRect)
        lockLastRect.set(lockBaseRect)
        lockPopup?.dismiss()
        lockPopup = null
        micPopup?.dismiss()
        micPopup = null
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.i(TAG, "[onLayout] w: $width, h: $height")
    }

    private fun cancel() {
        Log.e(TAG, "[cancel] no args")
        resetUI()
        state = RecordingState.Idle
        onStateChangeListener?.invoke(RecordingState.Idle)

        mediaRecorder.stopRecording()
        mediaRecorder.release()
    }

    private fun hold() {
        Log.e(TAG, "[hold] no args")
        state = RecordingState.Hold

        binding.horizontalGuideline.setGuidelinePercent(1f)
        layoutParams.height = centerContentHeight
        binding.recordingSlider.isVisible = true

        binding.recordingWaveform.isVisible = false
        binding.recordingStop.isVisible = false
        binding.recordingDelete.isVisible = false
        binding.recordingComplete.isVisible = false
        //TODO binding.recordingTimer.base = 0L
        //TODO binding.recordingTimer.start()

        Log.i(TAG, "[doOnPreDraw] w: ${binding.root.width}, h: ${binding.root.height}")

        mediaRecorder.startAudioRecording(context, recordingName = "audio_recording_${Date()}")

        mediaRecorder.setOnInfoListener(::onRecorderInfo)
        mediaRecorder.setOnErrorListener(::onRecorderError)
        mediaRecorder.setOnMediaRecorderStateChangedListener(::onRecorderStateChanged)
        mediaRecorder.setOnMaxAmplitudeSampledListener(::onRecorderMaxAmplitudeSampled)
        mediaRecorder.setOnCurrentRecordingDurationChangedListener(::onRecorderDurationChanged)
        mediaRecorder.setOnRecordingStoppedListener(::onRecorderStopped)
    }

    private fun onRecorderStopped() {
        Log.i(TAG, "[onRecorderStopped] no args")
    }

    private fun onRecorderDurationChanged(durationMs: Long) {
        logger.v { "[onRecorderDurationChanged] duration: $durationMs" }
        post { binding.recordingTimer.text = formatMillis(durationMs) }
    }

    private fun onRecorderStateChanged(mediaRecorderState: MediaRecorderState) {
        Log.i(TAG, "[onRecorderStateChanged] state: $mediaRecorderState")
    }

    private fun onRecorderInfo(streamMediaRecorder: StreamMediaRecorder, what: Int, extra: Int) {
        Log.i(TAG, "[onRecorderInfo] what: $what, extra: $extra")
    }

    private fun onRecorderError(streamMediaRecorder: StreamMediaRecorder, what: Int, extra: Int) {
        Log.e(TAG, "[onRecorderError] what: $what, extra: $extra")
    }

    private val waveformData = arrayListOf<Float>()
    private fun onRecorderMaxAmplitudeSampled(maxAmplitude: Int) {
        Log.v(TAG, "[onRecorderMaxAmplitudeSampled] maxAmplitude: $maxAmplitude")
        post {
            waveformData.add(maxAmplitude.toFloat() / 32767f)
            binding.recordingWaveform.waveBars = waveformData
        }
    }

    private fun lock() {
        Log.e(TAG, "[cancel] no args")
        state = RecordingState.Locked

        binding.horizontalGuideline.setGuidelinePercent(0.5f)
        layoutParams.height = layoutParams.height * 2
        binding.recordingSlider.isVisible = false

        binding.recordingWaveform.isVisible = true
        binding.recordingStop.isVisible = true
        binding.recordingDelete.isVisible = true
        binding.recordingComplete.isVisible = true

        micPopup?.dismiss()
        micPopup = null
        (lockPopup?.contentView as? ImageView?)?.setImageResource(R.drawable.stream_ui_ic_mic_locked_light)

        lockPopup?.update(lockBaseRect.left, lockBaseRect.top - 16.dpToPx(), -1, 64.dpToPx())

        onStateChangeListener?.invoke(RecordingState.Locked)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isVisible && state != RecordingState.Hold) {
            Log.w(TAG, "[onTouchEvent] rejected (not unlocked): $state")
            return false
        }
        Log.v(TAG, "[onTouchEvent] event: $event")
        val x = event.rawX
        val y = event.rawY
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "[onTouchEvent] ACTION_DOWN")
                if (!permissionChecker.isGrantedAudioRecordPermission(context)) {
                    permissionChecker.checkAudioRecordPermissions(this)
                    return false
                }
                parentWidth = composerContext.content.asView().width
                parentHeight = composerContext.content.asView().height
                centerContentHeight =
                    composerContext.content.center?.asView()?.height ?: error("no center content found")
                val recordAudioButton =
                    composerContext.content.findViewByKey(MessageComposerContent.RECORD_AUDIO_BUTTON)
                        ?: composerContext.content.trailing?.asView()?.findViewById(R.id.recordAudioButton)
                        ?: error("recordAudioButton not found")
                recordAudioButton.getRectInWindow(micOrigRect)
                showMicPopup()
                showLockPopup()
                hold()

                baseTouch[0] = x
                baseTouch[1] = y

                state = RecordingState.Hold
                onStateChangeListener?.invoke(RecordingState.Hold)
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = (x - baseTouch[0]).toInt()
                val deltaY = (y - baseTouch[1]).toInt()

                if (micBaseRect.width() == 0 || lockBaseRect.width() == 0) {
                    Log.v(TAG, "[onTouchEvent] ACTION_MOVE rejected (no popups 1)")
                    return false
                }
                if (micPopup == null || lockPopup == null) {
                    Log.v(TAG, "[onTouchEvent] ACTION_MOVE rejected (no popups 2)")
                    return false
                }

                Log.i(TAG, "[onTouchEvent] ACTION_MOVE: offsetX: $deltaX, offsetY: $deltaY")

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
                Log.w(TAG, "[onMove] progress: $progress, diff: ${micLastRect.left - micBaseRect.left}")
                binding.recordingSlider.translationX = micLastRect.left - micBaseRect.left.toFloat()
                binding.recordingSlider.alpha = 1 - progress * 1.5f

                if (micLastRect.left == micMoveRect.left) {
                    Log.w(TAG, "[onMove] cancelled; micLastRect: $micLastRect, micMoveRect: $micMoveRect")
                    cancel()
                    return false
                }

                if (micLastRect.top == micMoveRect.top) {
                    Log.w(TAG, "[onMove] locked")
                    lock()
                    return false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "[onTouchEvent] ACTION_UP")
                cancel()
                return true
            }
        }
        return true
    }

    private fun showMicPopup() {
        Log.d(TAG, "[showMicPopup] orig: $micOrigRect")
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
        Log.d(TAG, "[showLockPopup] micOrigRect: $micOrigRect")
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