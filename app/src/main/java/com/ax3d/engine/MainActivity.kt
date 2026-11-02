package com.ax3d.engine
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.ax3d.engine.ui.HomeScreen
import com.ax3d.engine.ui.GameControllerOverlay
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

        window.statusBarColor = 0xFF050A1D.toInt()
        window.navigationBarColor = 0xFF050A1D.toInt()

        importedGames.clear()
        File(filesDir, "games").listFiles()
            ?.filter { it.isFile && it.extension == "abx" }
            ?.let { importedGames.addAll(it) }

        game = importedGames.lastOrNull()

        status = TextView(this)
        gameLabel = TextView(this)
        library = TextView(this)

        status.text = "Prêt.\n" + nativeGetBuildInfo()
        gameLabel.text = game?.name ?: "Aucun jeu ABX importé"
        library.text = importedGames.joinToString("\n") { it.name }

        showHomeScreen()
    }

    private fun showHomeScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        val home = HomeScreen(
            context = this,
            onOpenGame = { openPicker() },
            onCreateProject = {
                Toast.makeText(this, "Création de projets bientôt disponible.", Toast.LENGTH_SHORT).show()
            },
            onRun = { launchGame() },
            onAssistant = {
                Toast.makeText(this, "Assistant AX3D bientôt disponible.", Toast.LENGTH_SHORT).show()
            },
            onSettings = {
                Toast.makeText(this, "Réglages AX3D bientôt disponibles.", Toast.LENGTH_SHORT).show()
            },
            onProjects = {
                Toast.makeText(this, "Bibliothèque des projets bientôt disponible.", Toast.LENGTH_SHORT).show()
            }
        )

        setContentView(
            home.build(
                nativeInfo = nativeGetBuildInfo(),
                recentProjects = importedGames.map { it.name }.reversed()
            )
        )
    }

    private fun showGameScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val controls = GameControllerOverlay(
            context = this,
            onExit = {
                nativeStop()
                showHomeScreen()
            },
            onInput = { button, pressed ->
                // Liaison avec les commandes natives à ajouter dans une prochaine étape.
            }
        )

        setContentView(controls)
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
        val f = game

        if (f == null || !f.exists()) {
            Toast.makeText(
                this,
                "Importez d'abord un fichier .abx.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        worker.execute {
            try {
                val result = nativeOpenGame(f.absolutePath)

                runOnUiThread {
                    Toast.makeText(
                        this,
                        result,
                        Toast.LENGTH_LONG
                    ).show()
                }

                if (result.startsWith("ABX GAME READY")) {
                    val frame = nativeRunFrame()

                    runOnUiThread {
                        showGameScreen()
                        Toast.makeText(this, frame, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Erreur : ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy(){ worker.execute{nativeStop()};worker.shutdown();super.onDestroy() }
}
