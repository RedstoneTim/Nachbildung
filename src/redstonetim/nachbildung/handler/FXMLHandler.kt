package redstonetim.nachbildung.handler

import javafx.fxml.FXMLLoader
import javafx.scene.Scene

object FXMLHandler {
    private val mainScene = javaClass.getResource("main_window.fxml")
    private val reconstructionNode = javaClass.getResource("reconstruction_node.fxml")
    private val solveNode = javaClass.getResource("solve_node.fxml")

    fun loadMainScene(): Scene {
        return FXMLLoader.load(mainScene)
    }
}