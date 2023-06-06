package io.getstream.chat.android.ui.feature.messages.composer.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.databinding.StreamUiMessageComposerDefaultOverlappingContentBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import kotlin.math.roundToInt

private const val TAG = "OverlappingContent"

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

    private var navBarHeight: Int

    private lateinit var binding: StreamUiMessageComposerDefaultOverlappingContentBinding

    public var onStateChangeListener: ((state: RecordingState) -> Unit)? = null

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        navBarHeight = context.getNavigationBarHeight()
    }

    private val parentView: ViewGroup by lazy(LazyThreadSafetyMode.NONE) { parent as ViewGroup }

    init {
        val inflater = LayoutInflater.from(context)
        binding = StreamUiMessageComposerDefaultOverlappingContentBinding.inflate(inflater, this)

        navBarHeight = context.getNavigationBarHeight()
    }

    override fun attachContext(messageComposerContext: MessageComposerContext) {
        // TODO
    }

    override fun renderState(state: MessageComposerState) {
        // TODO
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parentView.isVisible = false
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
            unlock()
        } else {
            Log.e(TAG, "[onVisibilityChanged] NOT_VISIBLE")
            resetUI()
        }
    }

    private fun resetUI() {
        Log.e(TAG, "[resetUI] no args")

        binding.recordingSlider.translationX = 0f
        binding.recordingSlider.alpha = 1f
        micLastRect.set(micBaseRect)
        lockLastRect.set(lockBaseRect)
        lockPopup?.dismiss()
        micPopup?.dismiss()
    }

    private fun cancel() {
        Log.e(TAG, "[cancel] no args")
        resetUI()
        state = RecordingState.Idle
        onStateChangeListener?.invoke(RecordingState.Idle)
    }

    private fun unlock() {
        Log.e(TAG, "[unlock] no args")
        state = RecordingState.Hold

        binding.recordingSlider.isVisible = true
        //binding.recordingStart.isVisible = true
        binding.recordingStart.visibility = View.INVISIBLE

        binding.recordingWaveform.isVisible = false
        binding.recordingStop.isVisible = false
        binding.recordingDelete.isVisible = false
        binding.recordingComplete.isVisible = false

        binding.recordingIndicator.doOnPreDraw {
            Log.e(TAG, "[doOnPreDraw] left: ${binding.recordingStart.left}, top: ${binding.recordingStart.top}")
            micXY.fetchLocationInWindow()
            showLockPopup(micXY)
            showMicPopup(micXY)
        }
    }

    private fun lock() {
        Log.e(TAG, "[cancel] no args")
        state = RecordingState.Locked

        binding.recordingSlider.isVisible = false
        binding.recordingStart.isVisible = false

        binding.recordingWaveform.isVisible = true
        binding.recordingStop.isVisible = true
        binding.recordingDelete.isVisible = true
        binding.recordingComplete.isVisible = true

        micPopup?.dismiss()
        (lockPopup?.contentView as? ImageView?)?.setImageResource(R.drawable.stream_ui_ic_mic_locked_light)

        lockPopup?.update(lockBaseRect.left, lockBaseRect.top - 16.dpToPx(), -1, 64.dpToPx())

        onStateChangeListener?.invoke(RecordingState.Locked)
    }

    private var lockPopup: PopupWindow? = null
    private val lockBaseRect = Rect()
    private val lockLastRect = Rect()
    private val lockMoveRect = Rect()


    private var micPopup: PopupWindow? = null
    private val micXY = IntArray(size = 2)
    private val micBaseRect = Rect()
    private val micLastRect = Rect()
    private val micMoveRect = Rect()

    private val baseTouch = FloatArray(size = 2)

    private var state: RecordingState = RecordingState.Hold
        set(value) {
            Log.e(TAG, "[setState] value: $value")
            field = value
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (parentView.isVisible && state != RecordingState.Hold) {
            Log.w(TAG, "[onTouchEvent] rejected (not unlocked): $state")
            return false
        }
        Log.v(TAG, "[onTouchEvent] event: $event")
        val x = event.rawX
        val y = event.rawY
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "[onTouchEvent] ACTION_DOWN: ${binding.recordingStart.left}")
                baseTouch[0] = x
                baseTouch[1] = y

                state = RecordingState.Hold
                onStateChangeListener?.invoke(RecordingState.Hold)
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = (x - baseTouch[0]).toInt()
                val deltaY = (y - baseTouch[1]).toInt()

                if (micBaseRect.width() == 0 || lockBaseRect.width() == 0) {
                    Log.v(TAG, "[onTouchEvent] ACTION_MOVE rejected (no popups)")
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
                    Log.w(TAG, "[onMove] cancelled")
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
                resetUI()
                state = RecordingState.Idle
                onStateChangeListener?.invoke(RecordingState.Idle)
                return true
            }
        }
        return true
    }

    private fun showLockPopup(micXY: IntArray) {
        Log.d(TAG, "[showLockPopup] no args")
        val lockW = binding.recordingStart.width
        val lockH = 128.dpToPx()
        lockPopup?.dismiss()
        lockPopup = PopupWindow(context).apply {
            //setBackgroundDrawable(ColorDrawable(Color.RED))
            setBackgroundDrawable(null)
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER
                setImageResource(R.drawable.stream_ui_ic_mic_lock_light)
            }
            contentView = imageView
            width = lockW
            height = lockH

            val lockLeft = micXY[0]
            val lockTop = micXY[1] - 128.dpToPx()
            showAtLocation(binding.root, Gravity.TOP or Gravity.START, lockLeft, lockTop)
            lockBaseRect.apply {
                left = lockLeft
                top = lockTop
                right = lockLeft + lockW
                bottom = lockTop + lockH
            }
            lockLastRect.set(lockBaseRect)
            lockMoveRect.apply {
                set(lockBaseRect)
                top -= binding.root.height * 2
            }
        }
    }

    private fun showMicPopup(micXY: IntArray) {
        Log.d(TAG, "[showMicPopup] no args")
        val micW = binding.recordingStart.width
        val micH = binding.recordingStart.height
        micPopup?.dismiss()
        micPopup = PopupWindow(context).apply {
            setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER
                setImageResource(R.drawable.baseline_mic_24)
            }
            contentView = imageView
            width = micW
            height = micH

            val micLeft = micXY[0]
            val micTop = micXY[1]
            Log.v(
                TAG,
                "[showMicPopup] x(${binding.root.width}): ${micLeft}, y(${binding.root.height}): ${micTop}, navBarHeight: $navBarHeight"
            )

            showAtLocation(binding.root, Gravity.TOP or Gravity.START, micLeft, micTop)
            micBaseRect.apply {
                left = micLeft
                top = micTop
                right = micLeft + micW
                bottom = micTop + micH
            }
            micLastRect.set(micBaseRect)
            micMoveRect.apply {
                set(micBaseRect)
                left -= (binding.root.width / 3)
                top -= binding.root.height * 2
            }
        }
    }

    private fun IntArray.fetchLocationInWindow() {
        binding.root.getLocationInWindow(this)
        this[0] = this[0] + binding.root.width - binding.recordingStart.width
    }

}

/**
 * Transforms DP value integer to pixels, based on the screen density.
 */
internal fun Int.dpToPx(): Int = dpToPxPrecise().roundToInt()

/**
 * Uses the display metrics to transform the value of DP to pixels.
 */
internal fun Int.dpToPxPrecise(): Float = (this * displayMetrics().density)

/**
 * Fetches the current system display metrics based on [Resources].
 */
internal fun displayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics

private fun Context.getNavigationBarHeight(): Int {
    val resources: Resources = resources

    val resName = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        "navigation_bar_height"
    } else {
        "navigation_bar_height_landscape"
    }

    val id: Int = resources.getIdentifier(resName, "dimen", "android")

    return if (id > 0) {
        resources.getDimensionPixelSize(id)
    } else {
        0
    }
}

private fun Int.limitTo(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}