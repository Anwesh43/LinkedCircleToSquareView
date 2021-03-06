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
val delay : Long = 20

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
    drawHalfSquare(size, h, paint)
}

fun Canvas.drawCTSCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
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
        paint.style = Paint.Style.FILL
        drawHalfSquareCircle(sc1.divideScale(j, rects), size, paint)
        paint.style = Paint.Style.STROKE
        drawHalfSquareCircle(sc1.divideScale(j, rects), size, paint)
        restore()
    }
    restore()
}

class CircleToSquareCircleView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, rects, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CTSCNode(var i : Int, val state : State = State()) {

        private var next : CTSCNode? = null
        private var prev : CTSCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CTSCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCTSCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CTSCNode {
            var curr : CTSCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircleToSquareCircle(var i : Int) {
        private val root : CTSCNode = CTSCNode(0)
        private var curr : CTSCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleToSquareCircleView) {

        private val animator : Animator = Animator(view)
        private val ctsc : CircleToSquareCircle = CircleToSquareCircle(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            ctsc.draw(canvas, paint)
            animator.animate {
                ctsc.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ctsc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : CircleToSquareCircleView {
            val view : CircleToSquareCircleView = CircleToSquareCircleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}