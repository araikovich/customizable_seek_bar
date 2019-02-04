package com.example.customizableseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class CustomizableSeekBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_THUMB_SIZE = 35
        const val DEFAULT_MAX_PROGRESS = 100
        const val DEFAULT_PROGRESS_HEIGHT = 5f
    }

    private var onProgressChangedListener: ((centerX: Int, centerY: Int, width: Int, height: Int, progress: Int) -> (Unit))? = null
    private var onStartTrackingTouch: (() -> (Unit))? = null
    private var onStopTrackingTouch: ((Int) -> (Unit))? = null

    private var isPress = false

    private var thumbSize: Float = 0f

    private var thumb: Drawable = ResourcesCompat.getDrawable(resources, R.drawable.simple_thumb, null)!!

    private var colorProgressLine: Int = ContextCompat.getColor(context, R.color.colorDefaltProgress)

    private var progressBackgroundColor: Int = ContextCompat.getColor(context, R.color.light_grey)

    private var enableClickOnThumbOnly: Boolean = true

    private var progressHeight: Float = DEFAULT_PROGRESS_HEIGHT

    var maxProgress: Int = DEFAULT_MAX_PROGRESS
        set(progress) {
            onProgressChangedListener?.invoke(thumb.bounds.centerX(), thumb.bounds.centerY(), thumb.bounds.width(), thumb.bounds.height(), progress)
            field = progress
            invalidate()
        }

    var progress: Int = 0
        set(progress) {
            field = progress
            onProgressChangedListener?.invoke(thumb.bounds.centerX(), thumb.bounds.centerY(), thumb.bounds.width(), thumb.bounds.height(), progress)
            invalidate()
        }

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
        initAttributset(attrs)
    }

    private fun initAttributset(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomCanvasSeekBar)

        a?.apply {
            thumbSize = getDimensionPixelOffset(R.styleable.CustomCanvasSeekBar_thumbSize, DEFAULT_THUMB_SIZE).toFloat()
            thumb = getDrawable(R.styleable.CustomCanvasSeekBar_thumb) ?: ResourcesCompat.getDrawable(resources, R.drawable.simple_thumb, null)!!
            colorProgressLine = useOrDefault(
                    ContextCompat.getColor(
                            context,
                            R.color.colorDefaltProgress
                    )
            ) { getColor(R.styleable.CustomCanvasSeekBar_progressColor, it) }
            progressBackgroundColor = useOrDefault(
                    ContextCompat.getColor(
                            context,
                            R.color.light_grey
                    )
            ) { getColor(R.styleable.CustomCanvasSeekBar_progressBackgroundColor, it) }
            enableClickOnThumbOnly = getBoolean(R.styleable.CustomCanvasSeekBar_enableClickOnThumbOnly, true)
            progressHeight = (getDimensionPixelOffset(
                    R.styleable.CustomCanvasSeekBar_progressHeight,
                    DEFAULT_PROGRESS_HEIGHT.toInt()
            )).toFloat()
            maxProgress =
                    getInteger(R.styleable.CustomCanvasSeekBar_maxProgress, DEFAULT_MAX_PROGRESS)
            recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        paint.strokeWidth = progressHeight
        paint.color = colorProgressLine
        canvas?.drawLine(
                paddingStart.toFloat(),
                height / 2f,
                ((width - (paddingStart + paddingEnd)) * progress / maxProgress) + paddingStart.toFloat(),
                height / 2f,
                paint
        )
        paint.color = progressBackgroundColor
        canvas?.drawLine(
                ((width - (paddingStart + paddingEnd)) * progress / maxProgress) + paddingStart.toFloat(),
                height / 2f,
                width.toFloat() - paddingEnd,
                height / 2f,
                paint
        )
        drawThumb(canvas, progress)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enableClickOnlyOnThumb(event)) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumbSize *= 1.5f
                onStartTrackingTouch?.invoke()
                updateOnTouch(event)
            }
            MotionEvent.ACTION_MOVE -> updateOnTouch(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                thumbSize /= 1.5f
                onStopTrackingTouch?.invoke(progress)
                isPress = false
                invalidate()
            }
        }
        return true
    }

    private fun drawThumb(canvas: Canvas?, progress: Int) {
        val thumbHalfWidth = thumbSize.div(2).toInt()

        val left = (((progress * (width - (paddingStart + paddingEnd))) / maxProgress) + paddingStart) - thumbHalfWidth
        val top = (height / 2) - thumbHalfWidth
        val bottom = (height / 2) + thumbHalfWidth
        val right = left + (thumbHalfWidth * 2)
        thumb.setBounds(left, top, right, bottom)
        thumb.draw(canvas)
    }

    private fun updateOnTouch(event: MotionEvent) {
        when {
            event.x < paddingStart -> {
                progress = 0
                return
            }
            event.x > width - paddingEnd -> progress = maxProgress
            else -> {
                val progressFromClick = progressFromClick(event.x.toInt())
                isPress = true
                progress = progressFromClick
            }
        }
    }

    private fun enableClickOnlyOnThumb(event: MotionEvent): Boolean {
        if (enableClickOnThumbOnly) {
            return if ((event.x > thumb.bounds.right + 40 && !isPress) || (event.x < thumb.bounds.left - 40 && !isPress)
                    || (event.y < thumb.bounds.top - 40 && !isPress) || (event.y > thumb.bounds.bottom + 40 && !isPress)) {
                true
            } else {
                isPress = true
                false
            }
        }
        return false
    }

    fun addUpdateListener(
            onProgressChangedListener: ((centerX: Int, centerY: Int, width: Int, height: Int, progress: Int) -> (Unit))? = null,
            onStartTrackingTouch: (() -> (Unit))? = null,
            onStopTrackingTouch: ((Int) -> (Unit))? = null
    ) {
        this.onProgressChangedListener = onProgressChangedListener
        this.onStartTrackingTouch = onStartTrackingTouch
        this.onStopTrackingTouch = onStopTrackingTouch
    }


    private fun progressFromClick(x: Int): Int =
            ((maxProgress * (x - paddingStart)).toDouble() / (width - (paddingStart + paddingEnd))).roundToInt()

    private fun <T, R> T?.useOrDefault(default: R, usage: T.(R) -> R) = if (this == null) default else usage(default)
}