package com.infinity.chat

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object MessageBubble {
    fun create(context: Context, message: ChatMessage): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = if (message.role == "user") Gravity.END else Gravity.START
            setPadding(0, 7, 0, 7)
        }
        if (message.role != "user") {
            row.addView(TextView(context).apply {
                text = "∞  INFINITY"
                textSize = 10f
                letterSpacing = .16f
                setTextColor(0xFF75E8F7.toInt())
                typeface = Typeface.DEFAULT_BOLD
                setPadding(5, 0, 0, 6)
            })
        }
        val bubble = TextView(context).apply {
            text = message.content.ifEmpty { if (message.streaming) "● ● ●" else "" }
            textSize = 15.5f
            setTextColor(if (message.role == "user") 0xFFF4F7FF.toInt() else 0xFFE4E9F8.toInt())
            setLineSpacing(5f, 1.04f)
            setPadding(15, 12, 15, 12)
            background = GradientDrawable().apply {
                cornerRadius = 19f
                if (message.role == "user") {
                    setColor(0xFF263B68.toInt())
                    setStroke(1, 0xFF5278C5.toInt())
                } else {
                    setColor(0xFF121C35.toInt())
                    setStroke(1, 0xFF263957.toInt())
                }
            }
        }
        val maxWidth = (context.resources.displayMetrics.widthPixels * .84f).toInt()
        row.addView(bubble, LinearLayout.LayoutParams(maxWidth, LinearLayout.LayoutParams.WRAP_CONTENT))
        return row
    }
}
