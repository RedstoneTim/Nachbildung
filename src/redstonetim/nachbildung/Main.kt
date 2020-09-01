package redstonetim.nachbildung

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import redstonetim.nachbildung.gui.MainScene
import redstonetim.nachbildung.gui.fxml.FXMLHandler
import redstonetim.nachbildung.io.IOHandler
import java.awt.Desktop
import java.net.URL

// TODO: Use dialog/alert thing for dialogs/alerts (mainly setting and save or not)
/**
 * Main class of the application.
 */
class Main : Application() {
    companion object {
        const val TITLE = "Nachbildung"
        const val VERSION = "1.0.0"
        val instance: Main = Main()
        lateinit var stage: Stage
            private set

        @JvmStatic
        fun main(args: Array<String>) {
            instance.start(args)
        }
    }

    private fun start(args: Array<String>) = launch(*args)

    override fun start(stage: Stage) {
        Main.stage = stage

        stage.title = TITLE
        stage.scene = FXMLHandler.loadMainScene()

        // Load all necessary files
        IOHandler.loadFiles()

        stage.setOnCloseRequest { event ->
            for (reconstruction in MainScene.instance.getReconstructions()) {
                if (reconstruction.showSavePopup(event)) {
                    break
                }
            }
        }

        stage.show()
    }

    override fun stop() {
        IOHandler.saveFiles()
    }

    fun openLink(link: String) {
        try {
            Desktop.getDesktop().browse(URL(link).toURI())
        } catch (e: Exception) {
            val stage = Stage()
            stage.initModality(Modality.APPLICATION_MODAL)
            val textField = TextField(link)
            textField.isEditable = false
            stage.scene = Scene(textField)
            stage.title = TITLE
            stage.showAndWait()
        }
    }
}