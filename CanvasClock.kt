package com.example.custom_view.canvas

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.example.custom_view.R
import java.lang.Exception
import java.util.*

/**
 * TODO: document your custom view class.
 */
class CanvasClock : View {

    private var textPaint: TextPaint? = null//数字画笔
    private var circlePaint: Paint? = null//外圆画笔
    private var solidCirclePiant: Paint? = null//实心圆画笔
    private var hourPaint: Paint? = null//时针画笔
    private var minutePaint: Paint? = null//分针画笔
    private var secondPaint: Paint? = null//秒针画笔

    private var circleX: Float = 0f //时钟圆心X点
    private var circleY: Float = 0f //时钟圆心Y点
    private var strTime: Array<String> = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
    private var hours: Int = 0
    private var minute: Int = 0
    private var second: Int = 0
    private var isMorning: Boolean = false
    private var isStop: Boolean = false
    private var paintColor: Int = Color.BLACK
    private var paintWidth: Float = 5f
    private var clockOuterRadius: Float = 200f//表盘外圆半径

    private var clockInnerRadius: Float = 0f//表盘内圆半径
    private var hoursAngle: Float = 0f //时针的角度
    private var minuteAngle: Float = 0f //分针的角度
    private var secondAngle: Float = 0f //秒针的角度


    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.CanvasClock, defStyle, 0)
//        _defaultColor = a.getColor(
//                R.styleable.ViewCanvas_clockBgColor,
//                paintColor)
        a.recycle()
        setBackgroundColor(Color.WHITE)
        initPaint()
        initTimeAngle()
        startClock()
    }

    private fun initPaint() {

        circlePaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = paintColor
            strokeWidth = paintWidth
            isDither = true
            style = Paint.Style.STROKE
        }
        solidCirclePiant = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isDither = true
            style = Paint.Style.FILL
        }

        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.CENTER
            color = Color.BLACK
            isDither = true
            textSize = 70.toFloat()

        }
        hourPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = Color.BLACK
            //防抖动
            isDither = true
            //直线圆角
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 12.toFloat()
        }
        minutePaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = Color.BLACK
            //防抖动
            isDither = true
            //直线圆角
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 8.toFloat()
        }

        secondPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = Color.RED
            //防抖动
            isDither = true
            //直线圆角
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 4.toFloat()
        }
    }

    private fun initTimeAngle() {
        hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        minute = Calendar.getInstance().get(Calendar.MINUTE)
        second = Calendar.getInstance().get(Calendar.SECOND)
        if (hours >= 12) {
            hours -= 12
            isMorning = false
        } else {
            isMorning = true
        }
        secondAngle = second * 6f
        minuteAngle = minute * 6f
        hoursAngle = hours * 30f + minute * 0.5f + second * 30f / 3600f
    }


    private fun updateHourAngle() {
        //当前的角度加上每秒时针走的度数
        hoursAngle += (30 / 3600)
        if (hoursAngle >= 360) {
            hoursAngle = 0f
            hours = 0
        }
    }

    private fun updateMinuteAngle() {
        minuteAngle += (60 / 360)
        if (minuteAngle >= 360) {
            minuteAngle = 0f
            minute = 0
        }
    }

    private fun updateSecondAngle() {
        secondAngle += 6f
        second += 1
        if (secondAngle >= 360) {
            secondAngle = 0f
            second = 0
            minute += 1
            //一分钟同步一次时间
            initTimeAngle()
        } else if (secondAngle == 180f) {
            //半分钟同步一次时间
            initTimeAngle()
        }
    }

    fun startClock() {
        isStop = false
        Thread {
            kotlin.run {
                while (!isStop) {
                    try {
                        Thread.sleep(1000)
                        updateSecondAngle()
                        updateMinuteAngle()
                        updateHourAngle()
                        postInvalidate()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }.start()
    }

    public fun stop() {
        isStop = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        circleX = (width / 2).toFloat()
        circleY = (height / 2).toFloat()
        initClockParams(Math.min(width, height))
    }

    private fun initClockParams(size: Int) {
        clockOuterRadius = (size / 2f).toFloat() - paintWidth / 2f
        clockInnerRadius = (clockOuterRadius!! - 15)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClock(canvas)
    }

    private fun drawClock(canvas: Canvas) {
        //绘制外圆
        canvas.drawCircle(circleX, circleY, clockOuterRadius, circlePaint)
        //绘制内圆
        canvas.drawCircle(circleX, circleY, clockInnerRadius, circlePaint)
        //绘制圆心
        canvas.drawCircle(circleX, circleY, 10f, circlePaint)

        for (index in 0..59) {
            if (index % 5 == 0) {
                //绘制整点刻度
                circlePaint?.strokeWidth = 10f
                var paths = calPoints(index * 6, clockInnerRadius)
                canvas.drawLines(paths, circlePaint)
                //绘制整点数字
                var textPaths = calPoints(index * 6 - 60, clockInnerRadius - 40)

                canvas.drawText(strTime[index / 5], textPaths[0], textPaths[1] + 24f, textPaint)
                continue
            }
            //绘制分钟刻度
            circlePaint?.strokeWidth = 3f
            var paths = calPoints(index * 6, clockInnerRadius)
            canvas.drawLines(paths, circlePaint)
        }


        //时针
        canvas.save()
        canvas.rotate(hoursAngle, circleX, circleY)
        canvas.drawLine(circleX, circleY, circleX, circleY * 0.5f, hourPaint)
        canvas.restore()

        //分针
        canvas.save()
        canvas.rotate(minuteAngle, circleX, circleY)
        canvas.drawLine(circleX, circleY, circleX, circleY * 0.3f, minutePaint)
        solidCirclePiant?.color = Color.BLACK
        canvas.drawCircle(circleX, circleY, 20.toFloat(), solidCirclePiant)
        canvas.restore()
        //秒针
        canvas.save()
        canvas.rotate(secondAngle, circleX, circleY)//旋转的是画布,从而得到指针旋转的效果
        canvas.drawLine(circleX, circleY + 40, circleX, circleY * 0.1f, secondPaint)
        solidCirclePiant?.color = Color.RED
        canvas.drawCircle(circleX, circleY, 10.toFloat(), solidCirclePiant)
        canvas.restore()
    }

    /**
     * 通过改变角度，来获取刻度线段的开始位置和结束位置
     */
    private fun calPoints(angle: Int, innerRadius: Float): FloatArray {
        var path = FloatArray(4)
        path[0] = (circleX + innerRadius * Math.cos(angle * Math.PI / 180)).toFloat()
        path[1] = (circleY + innerRadius * Math.sin(angle * Math.PI / 180)).toFloat()
        path[2] = (circleX + clockOuterRadius * Math.cos(angle * Math.PI / 180)).toFloat()
        path[3] = (circleY + clockOuterRadius * Math.sin(angle * Math.PI / 180)).toFloat()
        return path
    }

}
