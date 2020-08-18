package redstonetim.nachbildung

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.stage.Stage
import redstonetim.nachbildung.handler.FXMLHandler
import redstonetim.nachbildung.io.IOHandler

class Main : Application() {
    companion object {
        const val TITLE = "Nachbildung"
        const val VERSION = "0.1.0"
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
        stage.scene = FXMLLoader.load(javaClass.getResource("main_scene.fxml"))

        stage.setOnCloseRequest { event ->
            for (reconstruction in MainScene.instance.getReconstructions()) {
                if (reconstruction.showSavePopup(event)) {
                    break
                }
            }
        }

        stage.show()
    }

    fun openLink(link: String) {
        HostServicesFactory.getInstance(this).showDocument(link)
    }
}