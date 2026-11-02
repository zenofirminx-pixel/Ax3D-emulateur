package com.ax3d.engine.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import kotlin.math.*

class GameControllerOverlay(
    context: Context,
    private val onExit: () -> Unit,
    private val onInput: (String, Boolean) -> Unit
) : FrameLayout(context) {

    private val controller = ControllerView(context, onInput)

    init {
        setBackgroundColor(Color.rgb(5, 9, 24))

        addView(controller, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ))

        val exit = TextView(context).apply {
            text = "‹  QUITTER"
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(0xAA101D38.toInt())
                cornerRadius = 28f
                setStroke(2, 0xFF25D9FF.toInt())
            }
            setPadding(24, 12, 24, 12)
            setOnClickListener { onExit() }
        }

        addView(exit, LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            android.view.Gravity.TOP or android.view.Gravity.START
        ).apply {
            setMargins(18, 14, 0, 0)
        })
    }

    fun releaseAll() {
        controller.releaseAll()
    }

    private class ControllerView(
        context: Context,
        private val onInput: (String, Boolean) -> Unit
    ) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val active = mutableSetOf<String>()
        private var joyX = 0f
        private var joyY = 0f
        private var joyPointer = -1

        private data class Button(
            val name: String,
            val label: String,
            val color: Int,
            var x: Float = 0f,
            var y: Float = 0f,
            var pressed: Boolean = false
        )

        private val buttons = listOf(
            Button("triangle", "△", 0xFF45E69A.toInt()),
            Button("circle", "○", 0xFFFF526D.toInt()),
            Button("cross", "✕", 0xFF38BFFF.toInt()),
            Button("square", "□", 0xFFFF78C8.toInt())
        )

        private var joyCenterX = 0f
        private var joyCenterY = 0f
        private var joyRadius = 0f
        private var knobX = 0f
        private var knobY = 0f

        init {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()

            paint.style = Paint.Style.FILL
            paint.color = 0xFF070F27.toInt()
            canvas.drawColor(paint.color)

            // Zone centrale de rendu
            paint.color = 0xFF0B1732.toInt()
            canvas.drawRoundRect(
                RectF(w * .25f, h * .12f, w * .75f, h * .88f),
                22f, 22f, paint
            )

            paint.color = 0xFF8AA6C8.toInt()
            paint.textSize = 16f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "ZONE DE RENDU AX3D",
                w / 2f, h / 2f,
                paint
            )

            // Joystick gauche
            joyCenterX = w * .13f
            joyCenterY = h * .72f
            joyRadius = min(w, h) * .105f

            paint.color = 0x5537CFFF
            canvas.drawCircle(joyCenterX, joyCenterY, joyRadius * 1.35f, paint)

            paint.color = 0xAA132C50.toInt()
            canvas.drawCircle(joyCenterX, joyCenterY, joyRadius, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = 0xFF38CFFF.toInt()
            canvas.drawCircle(joyCenterX, joyCenterY, joyRadius, paint)

            paint.style = Paint.Style.FILL
            knobX = joyCenterX + joyX * joyRadius * .65f
            knobY = joyCenterY + joyY * joyRadius * .65f

            paint.color = 0x8838CFFF.toInt()
            canvas.drawCircle(knobX, knobY, joyRadius * .48f, paint)

            paint.color = 0xFFB9F4FF.toInt()
            canvas.drawCircle(knobX, knobY, joyRadius * .27f, paint)

            // Boutons à droite
            val r = min(w, h) * .052f
            val baseX = w * .86f
            val baseY = h * .69f
            val gap = r * 2.35f

            buttons[0].x = baseX
            buttons[0].y = baseY - gap
            buttons[1].x = baseX + gap
            buttons[1].y = baseY
            buttons[2].x = baseX
            buttons[2].y = baseY + gap
            buttons[3].x = baseX - gap
            buttons[3].y = baseY

            buttons.forEach { b ->
                val scale = if (b.pressed) 1.22f else 1f
                val radius = r * scale

                paint.style = Paint.Style.FILL
                paint.color = if (b.pressed) b.color else 0xAA14213C.toInt()
                canvas.drawCircle(b.x, b.y, radius * 1.25f, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = if (b.pressed) 5f else 3f
                paint.color = b.color
                canvas.drawCircle(b.x, b.y, radius, paint)

                paint.style = Paint.Style.FILL
                paint.color = if (b.pressed) Color.WHITE else b.color
                paint.textSize = radius * 1.15f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(
                    b.label,
                    b.x,
                    b.y + radius * .38f,
                    paint
                )
            }

            paint.style = Paint.Style.FILL
            paint.color = 0xFF9BB6D9.toInt()
            paint.textSize = 11f
            canvas.drawText("ANALOG", joyCenterX, joyCenterY + joyRadius * 1.65f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    val index = event.actionIndex
                    handlePress(event.getX(index), event.getY(index), event.getPointerId(index))
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        handleMove(event.getX(i), event.getY(i), event.getPointerId(i))
                    }
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_POINTER_UP -> {
                    val index = event.actionIndex
                    releasePointer(event.getPointerId(index))
                }

                MotionEvent.ACTION_CANCEL -> releaseAll()
            }
            invalidate()
            return true
        }

        private fun handlePress(x: Float, y: Float, pointer: Int) {
            val r = min(width, height) * .052f

            val button = buttons.firstOrNull {
                hypot(x - it.x, y - it.y) < r * 1.6f
            }

            if (button != null) {
                button.pressed = true
                active.add(button.name)
                onInput(button.name, true)
                return
            }

            if (hypot(x - joyCenterX, y - joyCenterY) < joyRadius * 1.7f &&
                joyPointer == -1
            ) {
                joyPointer = pointer
                updateJoystick(x, y)
            }
        }

        private fun handleMove(x: Float, y: Float, pointer: Int) {
            if (pointer == joyPointer) updateJoystick(x, y)
        }

        private fun updateJoystick(x: Float, y: Float) {
            val dx = x - joyCenterX
            val dy = y - joyCenterY
            val distance = hypot(dx, dy).coerceAtMost(joyRadius)

            joyX = if (distance == 0f) 0f else dx / hypot(dx, dy) * distance / joyRadius
            joyY = if (distance == 0f) 0f else dy / hypot(dx, dy) * distance / joyRadius

            onInput("analog_x", joyX > 0.15f)
            onInput("analog_y", joyY > 0.15f)
        }

        private fun releasePointer(pointer: Int) {
            if (pointer == joyPointer) {
                joyPointer = -1
                joyX = 0f
                joyY = 0f
                onInput("analog_x", false)
                onInput("analog_y", false)
            }

            buttons.forEach {
                if (it.pressed) {
                    it.pressed = false
                    active.remove(it.name)
                    onInput(it.name, false)
                }
            }
        }

        fun releaseAll() {
            buttons.forEach {
                if (it.pressed) onInput(it.name, false)
                it.pressed = false
            }
            joyPointer = -1
            joyX = 0f
            joyY = 0f
            onInput("analog_x", false)
            onInput("analog_y", false)
            invalidate()
        }
    }
}
