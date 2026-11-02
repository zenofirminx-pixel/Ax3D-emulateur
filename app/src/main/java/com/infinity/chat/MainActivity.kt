package com.infinity.chat

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val api = InfinityApi()
    private val executor = Executors.newSingleThreadExecutor()
    private val history = mutableListOf<ChatMessage>()
    private lateinit var messageList: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private lateinit var send: TextView
    private lateinit var status: TextView
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF080D20.toInt()
        window.navigationBarColor = 0xFF080D20.toInt()
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        buildInterface()
        addWelcome()
    }

    private fun buildInterface() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF080D20.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 13, 18, 13)
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(0xFF111D3B.toInt(), 0xFF0B1228.toInt()))
        }
        val logo = TextView(this).apply {
            text = "∞"
            textSize = 35f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setTextColor(0xFF72E8F7.toInt())
            gravity = Gravity.CENTER
        }
        header.addView(logo, LinearLayout.LayoutParams(48, 48))
        val titleBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10, 0, 0, 0) }
        titleBox.addView(TextView(this).apply {
            text = "l'Infinity"; textSize = 21f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFFF2F6FF.toInt())
        })
        titleBox.addView(TextView(this).apply {
            text = "INTELLIGENCE • STREAMING EN DIRECT"; textSize = 9f; letterSpacing = .12f
            setTextColor(0xFF7E9ABF.toInt())
        })
        header.addView(titleBox, LinearLayout.LayoutParams(0, -2, 1f))
        val online = TextView(this).apply {
            text = "●"; textSize = 12f; setTextColor(0xFF56E6B2.toInt())
            gravity = Gravity.CENTER
        }
        header.addView(online, LinearLayout.LayoutParams(24, 40))
        root.addView(header)

        status = TextView(this).apply {
            text = "CONNECTÉ À L'ESPACE INFINITY"
            textSize = 9f; letterSpacing = .1f; setTextColor(0xFF617A9E.toInt())
            setPadding(22, 9, 22, 3)
        }
        root.addView(status)

        scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(16, 8, 16, 12)
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        messageList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(messageList, ScrollView.LayoutParams(-1, -2))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composerWrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(13, 9, 13, 8)
            background = GradientDrawable().apply {
                setColor(0xFF0D1730.toInt())
                setStroke(1, 0xFF263957.toInt())
                cornerRadii = floatArrayOf(20f,20f,20f,20f,0f,0f,0f,0f)
            }
        }
        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_VERTICAL
        }
        input = EditText(this).apply {
            hint = "Écrivez votre message…"
            setHintTextColor(0xFF7285A5.toInt())
            setTextColor(0xFFF0F4FF.toInt())
            textSize = 15f
            setPadding(15, 12, 12, 12)
            minHeight = 48
            maxLines = 5
            setSingleLine(false)
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = GradientDrawable().apply {
                setColor(0xFF111E3A.toInt())
                cornerRadius = 17f
                setStroke(1, 0xFF2C4266.toInt())
            }
        }
        composer.addView(input, LinearLayout.LayoutParams(0, -2, 1f))
        send = TextView(this).apply {
            text = "➤"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(0xFF06101F.toInt())
            background = GradientDrawable().apply {
                setColor(0xFF70E6F3.toInt())
                cornerRadius = 16f
            }
            contentDescription = "Envoyer le message"
        }
        val sendParams = LinearLayout.LayoutParams(52, 50).apply { leftMargin = 9 }
        composer.addView(send, sendParams)
        composerWrap.addView(composer)
        composerWrap.addView(TextView(this).apply {
            text = "Infinity peut faire des erreurs. Vérifiez les réponses importantes."
            textSize = 10f; gravity = Gravity.CENTER; setTextColor(0xFF607494.toInt())
            setPadding(0, 7, 0, 0)
        })
        root.addView(composerWrap)

        setContentView(root)
        send.setOnClickListener { submitMessage() }
        input.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) { submitMessage(); true } else false
        }
    }

    private fun addWelcome() {
        val welcome = ChatMessage("assistant", "Bienvenue dans l'Infinity.\n\nJe suis prêt à discuter avec vous. Envoyez un message pour commencer.")
        history.add(welcome)
        renderMessage(welcome)
    }

    private fun submitMessage() {
        if (busy) return
        val prompt = input.text.toString().trim()
        if (prompt.isEmpty()) return
        input.setText("")
        history.add(ChatMessage("user", prompt))
        renderMessage(history.last())
        val answer = ChatMessage("assistant", "", streaming = true)
        history.add(answer)
        renderMessage(answer)
        busy = true
        send.isEnabled = false
        send.alpha = .5f
        status.text = "TRANSMISSION EN COURS • STREAM SSE"

        val contextHistory = history.dropLast(2).map { ChatMessage(it.role, it.content) }
        executor.execute {
            try {
                api.streamReply(prompt, contextHistory) { token ->
                    runOnUiThread {
                        answer.content += token
                        updateLastBubble(answer)
                        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
                    }
                }
                runOnUiThread {
                    answer.content = answer.content.ifBlank { "Le serveur a terminé sans renvoyer de texte." }
                    finishAnswer(answer)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    answer.content = "Impossible de contacter Infinity : ${e.message ?: "erreur réseau"}"
                    finishAnswer(answer)
                    status.text = "CONNEXION INTERROMPUE"
                }
            }
        }
    }

    private fun renderMessage(message: ChatMessage) {
        messageList.addView(MessageBubble.create(this, message))
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun updateLastBubble(message: ChatMessage) {
        if (messageList.childCount > 0) {
            messageList.removeViewAt(messageList.childCount - 1)
            messageList.addView(MessageBubble.create(this, message))
        }
    }

    private fun finishAnswer(answer: ChatMessage) {
        updateLastBubble(answer)
        busy = false
        send.isEnabled = true
        send.alpha = 1f
        status.text = "CONNECTÉ À L'ESPACE INFINITY"
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
