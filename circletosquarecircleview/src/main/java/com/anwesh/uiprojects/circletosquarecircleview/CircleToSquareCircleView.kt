package com.anwesh.uiprojects.circletosquarecircleview

/**
 * Created by anweshmishra on 02/04/19.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color

val nodes : Int = 5
val rects : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * scGap * dir

fun Canvas.drawSemiCircle(y : Float, r : Float, paint : Paint) {
    save()
    translate(0f, y)
    drawArc(RectF(-r, -r, r, r), 0f, 180f, true, paint)
    restore()
}

fun Canvas.drawHalfSquare(w : Float, h : Float, paint : Paint) {
    drawRect(RectF(-w / 2, 0f, w / 2, h), paint)
}

fun Canvas.drawHalfSquareCircle(sc : Float, size : Float, paint : Paint) {
    val h : Float = (size / 2) * sc
    drawSemiCircle(h, size / 2, paint)
    drawHalfSquare(h, size / 2, paint)
}

fun Canvas.drawCTSCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    for (j in 0..(rects - 1)) {
        save()
        scale(1f, 1f - 2 * j)
        drawHalfSquareCircle(sc1.divideScale(j, rects), size, paint)
        restore()
    }
    restore()
}

class CircleToSquareCircleView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}