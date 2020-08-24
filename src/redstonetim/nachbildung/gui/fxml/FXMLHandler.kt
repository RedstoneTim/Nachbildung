package redstonetim.nachbildung.gui.fxml

import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import redstonetim.nachbildung.gui.ReconstructionNode
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.setting.SettingsStage

object FXMLHandler {
    private val mainScene = javaClass.getResource("main_scene.fxml")
    private val reconstructionNode = javaClass.getResource("reconstruction_node.fxml")
    private val solveNode = javaClass.getResource("solve_node.fxml")
    private val settingsStage = javaClass.getResource("settings_stage.fxml")

    internal fun loadMainScene(): Scene {
        return FXMLLoader.load(mainScene)
    }

    internal fun loadReconstruction(): Pair<Node, ReconstructionNode> {
        val loader = FXMLLoader()
        return loader.load<Node>(reconstructionNode.openStream()) to loader.getController<ReconstructionNode>()
    }

    internal fun loadSolve(): Pair<Node, SolveNode> {
        val loader = FXMLLoader()
        return loader.load<Node>(solveNode.openStream()) to loader.getController<SolveNode>()
    }

    internal fun loadSettingsStage(): SettingsStage {
        val loader = FXMLLoader()
        loader.load<Stage>(settingsStage.openStream())
        return loader.getController<SettingsStage>()
    }
}