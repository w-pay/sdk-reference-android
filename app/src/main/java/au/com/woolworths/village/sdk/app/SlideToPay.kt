package au.com.woolworths.village.sdk.app

import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import au.com.woolworths.village.sdk.app.databinding.SlideToPayBinding

interface OnSwipedListener {
    fun onSwiped()
}

class SlideToPay: FrameLayout {
    /** Where the button can start */
    private var minButtonStart: Float = 0f

    /** The furthest point the button can move to **/
    private var maxButtonEnd: Float = 0f

    /** The position where the button is to be considered to have been "swiped" */
    private var swiped: Float = 0f

    /** Whether or not we can move the button */
    private var locked: Boolean = false

    var listener: OnSwipedListener? = null

    lateinit var bindings: SlideToPayBinding

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, -1)
    }

    @TargetApi(21)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun lock() {
        this.locked = true
    }

    fun unlock() {
        this.locked = false
    }

    @Suppress("UNUSED_PARAMETER")
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        bindings = SlideToPayBinding.inflate(LayoutInflater.from(context), this, true)

        setOnTouchListener(buttonTouchListener())
    }

    private fun buttonTouchListener(): OnTouchListener? {
        return OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    return@OnTouchListener true

                MotionEvent.ACTION_MOVE -> {
                    if (!locked) {
                        moveButton(event)
                    }

                    return@OnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    checkButtonLocation()

                    return@OnTouchListener true
                }
            }

            false
        }
    }

    private fun moveButton(event: MotionEvent) {
        val button = bindings.button

        /*
         * Initialise the values now that everything has been measured and positioned.
         */
        if (minButtonStart == 0f) {
            minButtonStart = button.x
        }

        if (maxButtonEnd == 0f) {
            maxButtonEnd = measuredWidth.toFloat() - minButtonStart
        }

        if (swiped == 0f) {
            swiped = maxButtonEnd - button.width
        }

        /*
         * The location of the event is in the middle of the button. So we need to move to either
         * the left (start) or right (end) of the button
         */
        val halfButtonWidth = button.width / 2

        // where we would like the right (end) hand side of the button to be.
        val newButtonEnd = event.x + halfButtonWidth

        // where we would like the left (start) hand side of the button to be.
        val newButtonStart = event.x - halfButtonWidth

        if (event.x > minButtonStart + halfButtonWidth &&
            newButtonEnd < maxButtonEnd
        ) {
            button.x = newButtonStart

            updateCenterTextAlpha()
        }

        if (newButtonEnd > maxButtonEnd) {
            button.x = maxButtonEnd - button.width.toFloat()
        }

        if (event.x < halfButtonWidth &&
            button.x > 0
        ) {
            button.x = minButtonStart
        }

        if (button.x >= swiped) {
            locked = true

            listener?.onSwiped()
        }
    }

    private fun checkButtonLocation() {
        val button = bindings.button

        if (button.x < swiped) {
            // snap the button back to the start
            button.x = minButtonStart

            updateCenterTextAlpha()
        }
    }

    private fun updateCenterTextAlpha() {
        val button = bindings.button

        bindings.centerText.alpha = 1 - 1.3f * (button.x + button.width) / maxButtonEnd
    }
}