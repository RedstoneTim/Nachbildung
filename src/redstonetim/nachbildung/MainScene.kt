package redstonetim.nachbildung

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import redstonetim.nachbildung.io.IOHandler

class MainScene {
    companion object {
        private val extensionFilters = arrayOf(FileChooser.ExtensionFilter("JSON files", "*.json"),
                FileChooser.ExtensionFilter("All files", "*.*"))
        lateinit var instance: MainScene
            private set
    }

    @FXML
    fun initialize() {
        instance = this
    }

    @FXML
    private lateinit var tabPane: TabPane

    fun addReconstruction(reconstruction: ReconstructionNode) {
        tabPane.tabs.add(reconstruction)
    }

    fun getReconstructions(): List<ReconstructionNode> = tabPane.tabs.mapNotNull { if (it is ReconstructionNode) it else null }

    fun getSelectedReconstruction(): ReconstructionNode? {
        val selectedItem = tabPane.selectionModel.selectedItem
        return if (selectedItem is ReconstructionNode) selectedItem else null
    }

    fun setSelectedReconstruction(reconstruction: ReconstructionNode) {
        tabPane.selectionModel.select(reconstruction)
    }

    @FXML
    private fun onMenuNew(event: ActionEvent) {
        // TODO: Custom FXML
        val newFile = Stage()
        newFile.initModality(Modality.APPLICATION_MODAL)
        newFile.title = Main.TITLE
        val name = TextField("Unnamed")
        name.promptText = "Name"
        val finishButton = Button("Finish")
        finishButton.setOnAction {
            val reconstruction = ReconstructionNode.create(name.text)
            reconstruction.addSolve(SolveNode.create(reconstruction))
            addReconstruction(reconstruction)
            setSelectedReconstruction(reconstruction)
            newFile.close()
        }
        val cancelButton = Button("Cancel")
        cancelButton.setOnAction { newFile.close() }
        val buttons = HBox(finishButton, cancelButton)
        buttons.alignment = Pos.CENTER
        val content = VBox(name, buttons)
        newFile.scene = Scene(content)
        newFile.showAndWait()
    }

    @FXML
    private fun onMenuOpen(event: ActionEvent) {
        IOHandler.getFileChooser("Open", *extensionFilters).showOpenMultipleDialog(Main.stage)?.let { IOHandler.openReconstructions(it) }
    }

    @FXML
    fun onMenuSave(event: ActionEvent) {
        getSelectedReconstruction()?.let {
            IOHandler.saveReconstructionAs(it, it.fileLocation
                    ?: IOHandler.getFileChooser("Save As...", *extensionFilters).showSaveDialog(Main.stage))
        }
    }

    @FXML
    private fun onMenuSaveAs(event: ActionEvent) {
        getSelectedReconstruction()?.let { IOHandler.saveReconstructionAs(it, IOHandler.getFileChooser("Save As...", *extensionFilters).showSaveDialog(Main.stage)) }
    }

    @FXML
    private fun onMenuWiki(event: ActionEvent) = Main.instance.openLink("https://github.com/RedstoneTim/Nachbildung/wiki")

    @FXML
    private fun onMenuFeedback(event: ActionEvent) = Main.instance.openLink("https://github.com/RedstoneTim/Nachbildung/issues")

    @FXML
    private fun onMenuAbout(event: ActionEvent) {
        // TODO: FXML
        val aboutStage = Stage()
        aboutStage.initModality(Modality.APPLICATION_MODAL)
        aboutStage.title = Main.TITLE
        aboutStage.scene = Scene(VBox(Label("${Main.TITLE} ${Main.VERSION}"), Label("RedstoneTim")))
        aboutStage.showAndWait()
    }
}