package com.ax3d.engine.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

class HomeScreen(
    private val context: Context,
    private val onOpenGame: () -> Unit,
    private val onCreateProject: () -> Unit,
    private val onRun: () -> Unit,
    private val onAssistant: () -> Unit,
    private val onSettings: () -> Unit,
    private val onProjects: () -> Unit
) {
    private val navy = Color.rgb(5, 10, 29)
    private val panel = Color.rgb(12, 22, 48)
    private val cyan = Color.rgb(35, 218, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(170, 190, 220)

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private fun background(
        color: Int,
        stroke: Int = 0,
        radius: Int = 18
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != 0) {
            setStroke(dp(1), stroke)
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = white,
        bold: Boolean = false
    ) = TextView(context).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }

    private fun button(
        label: String,
        action: () -> Unit
    ) = TextView(context).apply {
        text = label
        textSize = 14f
        gravity = Gravity.CENTER
        setTextColor(white)
        setPadding(dp(12), dp(14), dp(12), dp(14))
        background = background(panel, cyan, 15)
        isClickable = true
        isFocusable = true
        setOnClickListener { action() }
    }

    private fun card(
        title: String,
        subtitle: String,
        symbol: String
    ) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = background(panel, 0xFF243D69.toInt(), 16)

        addView(text(symbol, 27f, cyan, true))
        addView(text(title, 14f, white, true))
        addView(text(subtitle, 11f, muted))
    }

    fun build(
        nativeInfo: String,
        recentProjects: List<String>
    ): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(navy)
        }

        val scroll = ScrollView(context)
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(18))
        }

        // En-tête
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        brand.addView(text("AX3D", 32f, cyan, true))
        brand.addView(text("ENGINE • DREAM • DEVELOP • PLAY", 10f, muted))

        header.addView(brand)
        header.addView(text("♢", 25f, cyan))
        header.addView(
            TextView(context).apply {
                text = "  ◉  "
                textSize = 25f
                setTextColor(white)
            }
        )
        content.addView(header)

        content.addView(
            text("Bonjour, développeur ! 👋", 21f, white, true).apply {
                setPadding(0, dp(22), 0, dp(4))
            }
        )
        content.addView(
            text("Créez. Jouez. Explorez les univers 3D.", 13f, muted)
        )

        // Bannière principale
        val banner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(20))
            background = background(
                0xFF10264D.toInt(),
                0xFF168DDB.toInt(),
                22
            )
        }

        banner.addView(text("✦  UNIVERS AX3D  ✦", 11f, cyan, true))
        banner.addView(
            text("Bienvenue sur AX3D", 23f, white, true).apply {
                setPadding(0, dp(12), 0, dp(6))
            }
        )
        banner.addView(
            text(
                "Votre espace de création et d'exécution de projets 3D.",
                13f,
                muted
            )
        )

        banner.addView(
            button("▶   Lancer un projet") { onRun() }.apply {
                setPadding(dp(20), dp(15), dp(20), dp(15))
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            }
        )

        content.addView(
            banner,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22)
            }
        )

        // Actions rapides
        content.addView(
            text("Actions rapides", 18f, white, true).apply {
                setPadding(0, dp(24), 0, dp(12))
            }
        )

        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val open = button("▣\nOuvrir\nun jeu") { onOpenGame() }
        val create = button("＋\nCréer\nun projet") { onCreateProject() }
        val shop = button("◇\nBoutique\nde jeux") {
            Toast.makeText(
                context,
                "La boutique sera développée ultérieurement.",
                Toast.LENGTH_SHORT
            ).show()
        }

        listOf(open, create, shop).forEachIndexed { index, view ->
            val params = LinearLayout.LayoutParams(0, dp(105), 1f)
            if (index > 0) params.marginStart = dp(7)
            actions.addView(view, params)
        }

        content.addView(actions)

        // Projets récents
        val recentHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(25), 0, dp(12))
        }

        recentHeader.addView(
            text("Projets récents", 18f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        recentHeader.addView(
            text("Tout voir  ›", 12f, cyan).apply {
                setOnClickListener { onProjects() }
            }
        )
        content.addView(recentHeader)

        if (recentProjects.isEmpty()) {
            val empty = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(25), dp(16), dp(25))
                background = background(panel, 0xFF243D69.toInt())
            }
            empty.addView(text("＋", 32f, cyan, true))
            empty.addView(text("Aucun projet récent", 15f, white, true))
            empty.addView(
                text("Importez un fichier ABX pour commencer.", 12f, muted)
            )
            content.addView(empty)
        } else {
            recentProjects.take(5).forEach { name ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    background = background(panel, 0xFF243D69.toInt())
                }
                row.addView(text("▣", 25f, cyan))
                row.addView(
                    text(name, 14f, white, true).apply {
                        setPadding(dp(14), 0, 0, 0)
                    }
                )
                content.addView(
                    row,
                    LinearLayout.LayoutParams(-1, -2).apply {
                        bottomMargin = dp(8)
                    }
                )
            }
        }

        // Informations techniques
        content.addView(
            text("MOTEUR AX3D", 11f, cyan, true).apply {
                setPadding(0, dp(24), 0, dp(8))
            }
        )
        content.addView(
            text(nativeInfo, 11f, muted)
        )

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        // Navigation inférieure
        val navigation = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(9), dp(4), dp(9))
            background = background(
                0xFF09142D.toInt(),
                0xFF244C83.toInt(),
                18
            )
        }

        val tabs = listOf(
            "⌂\nAccueil" to {},
            "▣\nProjets" to onProjects,
            "▷\nExécuter" to onRun,
            "✧\nAssistant" to onAssistant,
            "⚙\nRéglages" to onSettings
        )

        tabs.forEachIndexed { index, tab ->
            val item = TextView(context).apply {
                text = tab.first
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(if (index == 0) cyan else muted)
                setPadding(0, dp(5), 0, dp(5))
                setOnClickListener { tab.second() }
            }
            navigation.addView(
                item,
                LinearLayout.LayoutParams(0, -2, 1f)
            )
        }

        root.addView(
            navigation,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(dp(10), dp(5), dp(10), dp(8))
            }
        )

        return root
    }
}
