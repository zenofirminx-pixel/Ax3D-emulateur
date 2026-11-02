package com.ax3d.engine

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.navigationBarColor = Color.rgb(5, 11, 32)
        window.statusBarColor = Color.rgb(5, 11, 32)

        setContentView(SplashView())

        android.os.Handler(mainLooper).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3200)
    }

    private inner class SplashView : View(this) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var progress = 0f
        private var pulse = 0f

        private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                pulse = it.animatedValue as Float
                invalidate()
            }
        }

        init {
            animator.start()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val cy = h * 0.43f

            canvas.drawColor(Color.rgb(5, 11, 32))

            // Halo spatial
            paint.shader = RadialGradient(
                cx, cy, w * 0.65f,
                intArrayOf(0x4430A9FF, 0x11102D65, 0x00050B20),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null

            // Étoiles
            for (i in 0..90) {
                val x = ((i * 127.7f) % w)
                val y = ((i * 83.3f) % h)
                paint.color = Color.argb(
                    (90 + 100 * ((i % 4) / 3f)).toInt(),
                    180, 220, 255
                )
                canvas.drawCircle(x, y, if (i % 5 == 0) 2f else 1f, paint)
            }

            // Planète
            paint.shader = RadialGradient(
                cx - 35f, cy - 45f, w * 0.29f,
                intArrayOf(0xFF4DDAFF, 0xFF1263B8, 0xFF071B45),
                null, Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, w * 0.25f, paint)
            paint.shader = null

            // Anneau lumineux avec animation Pulse
            val pulseScale = 0.5f + 0.5f *
                kotlin.math.sin(pulse * Math.PI).toFloat()

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f + pulseScale * 3f
            paint.color = Color.argb(
                (150 + pulseScale * 105).toInt(),
                37, 223, 255
            )
            paint.setShadowLayer(
                12f + pulseScale * 22f,
                0f, 0f, 0xFF25DFFF.toInt()
            )

            canvas.save()
            canvas.rotate(-18f, cx, cy)
            canvas.drawOval(
                cx - w * (0.36f + pulseScale * 0.008f),
                cy - w * 0.105f,
                cx + w * (0.36f + pulseScale * 0.008f),
                cy + w * 0.105f,
                paint
            )
            canvas.restore()

            paint.clearShadowLayer()
            paint.style = Paint.Style.FILL

            // Logo AX3D
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            paint.textSize = w * 0.17f
            paint.setShadowLayer(22f, 0f, 0f, 0xFF12BFFF.toInt())
            canvas.drawText("AX3D", cx, cy + w * 0.055f, paint)
            paint.clearShadowLayer()

            // Slogan
            paint.textSize = w * 0.033f
            paint.color = 0xFFB9D9F8.toInt()
            canvas.drawText("DREAM  •  DEVELOP  •  PLAY", cx, cy + w * 0.43f, paint)

            // Barre de chargement
            val barWidth = w * 0.58f
            val barY = h * 0.84f

            paint.color = 0xFF263B59.toInt()
            canvas.drawRoundRect(
                cx - barWidth / 2, barY,
                cx + barWidth / 2, barY + 7f,
                8f, 8f, paint
            )

            progress = (progress + 0.004f).coerceAtMost(1f)
            paint.color = 0xFF25DFFF.toInt()
            canvas.drawRoundRect(
                cx - barWidth / 2, barY,
                cx - barWidth / 2 + barWidth * progress, barY + 7f,
                8f, 8f, paint
            )

            paint.textSize = 12f
            paint.color = 0xFF8DAACB.toInt()
            canvas.drawText("INITIALIZING AX3D ENGINE", cx, barY + 32f, paint)

            if (progress < 1f) postInvalidateDelayed(30)
        }

        override fun onDetachedFromWindow() {
            animator.cancel()
            super.onDetachedFromWindow()
        }
    }
}
