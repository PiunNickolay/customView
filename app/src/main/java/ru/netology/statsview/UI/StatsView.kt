package ru.netology.statsview.UI

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()


    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()

            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor())
            )
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            val total = value.sum()
            field = if (total == 0F) {
                emptyList()
            } else {
                value.map { it / total }
            }
            startAnimation()
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private var progress = 0F
    private var rotationAngle = 0F

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - AndroidUtils.dp(context, 5)
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        canvas.save()
        canvas.rotate(rotationAngle, center.x, center.y)

        var startAngle = -90F
        var drawn = 0F

        data.forEachIndexed { index, datum ->
            val end = drawn + datum

            val partProgress = when {
                progress >= end -> 1F
                progress <= drawn -> 0F
                else -> (progress - drawn) / datum
            }

            val angle = datum * 360F * partProgress
            paint.color = colors.getOrElse(index) { generateRandomColor() }

            if (angle > 0F) {
                canvas.drawArc(
                    oval,
                    startAngle,
                    angle,
                    false,
                    paint
                )
            }
            startAngle += angle * 360f
            drawn = end
        }
        canvas.restore()

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun startAnimation(){
        val animator = ValueAnimator.ofFloat(0F, 1F)
        animator.duration = 2000L
        animator.addUpdateListener { anim->
            progress = anim.animatedValue as Float
            rotationAngle = 360F * progress
            invalidate()
        }
        animator.start()
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}