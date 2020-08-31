package redstonetim.nachbildung

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import javafx.application.Application
import javafx.stage.Stage
import redstonetim.nachbildung.gui.MainScene
import redstonetim.nachbildung.gui.fxml.FXMLHandler
import redstonetim.nachbildung.io.IOHandler

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

        // Load all necessary files
        IOHandler.loadFiles()

        stage.title = TITLE
        stage.scene = FXMLHandler.loadMainScene()

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
        HostServicesFactory.getInstance(this).showDocument(link)
    }
}