package com.ax3d.engine
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private external fun nativeOpenGame(path: String): String
    private external fun nativeRunFrame(): String
    private external fun nativeStop()
    private external fun nativeGetBuildInfo(): String
    private val worker = Executors.newSingleThreadExecutor()
    private var game: File? = null
    private val importedGames = mutableListOf<File>()
    private lateinit var status: TextView
    private lateinit var gameLabel: TextView
    private lateinit var library: TextView
    companion object { init { System.loadLibrary("ax3d") }; const val PICK = 4101 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF090A0E.toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(22,32,22,20)
            setBackgroundColor(0xFF090A0E.toInt())
        }
        root.addView(TextView(this).apply { text="AX3D ENGINE"; textSize=28f; setTextColor(0xFFFFC857.toInt()) })
        root.addView(TextView(this).apply { text="ABX Binary Runtime • Alpha 0.4"; setTextColor(0xFFB9BBC7.toInt()) })
        gameLabel = TextView(this).apply {
            text="Aucun jeu ABX importé"; textSize=17f; setTextColor(-1)
            setPadding(16,24,16,24); setBackgroundColor(0xFF171923.toInt())
        }
        root.addView(gameLabel)
        val add = makeButton("IMPORTER UN JEU .ABX")
        val start = makeButton("CHARGER ET EXÉCUTER")
        val stop = makeButton("ARRÊTER")
        root.addView(add); root.addView(start); root.addView(stop)
        root.addView(TextView(this).apply { text="BIBLIOTHÈQUE"; setTextColor(0xFFFFC857.toInt()); textSize=15f; setPadding(2,22,2,8) })
        library = TextView(this).apply { text="Aucun jeu dans la bibliothèque"; setTextColor(0xFFB9BBC7.toInt()); setPadding(12,10,12,10) }
        root.addView(library)
        val controls = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(0,16,0,8) }
        listOf("◀", "▲", "▼", "▶", "✕", "○").forEach { label ->
            val key = TextView(this).apply { text=label; textSize=20f; gravity=Gravity.CENTER; setTextColor(-1); setBackgroundColor(0xFF292D3A.toInt()); setPadding(14,12,14,12) }
            key.setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP }
            controls.addView(key, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(controls)
        importedGames.clear(); File(filesDir,"games").listFiles()?.filter { it.isFile }?.let { importedGames.addAll(it) }
        if(importedGames.isNotEmpty()) library.text=importedGames.joinToString("\n") { it.name }
        status = TextView(this).apply {
            text="Prêt.\n"+nativeGetBuildInfo(); setTextColor(0xFFB9BBC7.toInt())
            textSize=13f; setPadding(4,20,4,4)
        }
        root.addView(status)
        root.addView(TextView(this).apply {
            text="Format ABX v1 : signature AX3D, en-tête contrôlé, CRC32 et bytecode exécuté dans une VM isolée. Les jeux PSP ISO/CSO ne sont pas des jeux ABX et ne sont pas encore exécutables ici."
            textSize=12f; setTextColor(0xFF9296A5.toInt()); setPadding(4,18,4,4)
        })
        setContentView(ScrollView(this).apply { addView(root) })
        add.setOnClickListener { openPicker() }
        start.setOnClickListener { launchGame() }
        stop.setOnClickListener { worker.execute { nativeStop(); runOnUiThread { status.text="Émulation arrêtée." } } }
    }
    private fun makeButton(label:String)=Button(this).apply {
        text=label; isAllCaps=false; setTextColor(-1); setBackgroundColor(0xFF292D3A.toInt())
    }
    private fun openPicker() {
        val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE); type="application/octet-stream"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream","application/x-iso9660-image"))
        }
        startActivityForResult(i,PICK)
    }
    @Deprecated("Compatibility callback")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=PICK || resultCode!=RESULT_OK) return
        val uri:Uri=data?.data ?: return
        worker.execute {
            try {
                val name=contentResolver.query(uri,arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),null,null,null)?.use {
                    if(it.moveToFirst()) it.getString(0) else null
                } ?: "game.iso"
                val ext=if(name.lowercase().endsWith(".abx")) ".abx" else ".unsupported"
                require(ext==".abx") { "Cette version importe les exécutables .abx uniquement." }
                val dir=File(filesDir,"games").apply{mkdirs()}
                val target=File(dir,"game_${System.currentTimeMillis()}$ext")
                contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Fichier inaccessible" }
                    FileOutputStream(target).use { output -> input.copyTo(output,1024*1024) }
                }
                game=target
                if(!importedGames.contains(target)) importedGames.add(target)
                runOnUiThread { gameLabel.text="$name\n${target.length()/1048576} MiB • importé"; status.text="Paquet ABX importé. Signature et intégrité seront vérifiées au chargement."; library.text=importedGames.joinToString("\n") { it.name } }
            } catch(e:Exception) { runOnUiThread { status.text="Import impossible : ${e.message}" } }
        }
    }
    private fun launchGame() {
        val f=game
        if(f==null || !f.exists()){status.text="Importez d'abord un fichier .abx.";return}
        worker.execute {
            val result=nativeOpenGame(f.absolutePath)
            runOnUiThread { status.text=result }
            if(result.startsWith("ABX GAME READY")) {
                val frame=nativeRunFrame()
                runOnUiThread { status.append("\n\n$frame") }
            }
        }
    }
    override fun onDestroy(){ worker.execute{nativeStop()};worker.shutdown();super.onDestroy() }
}
