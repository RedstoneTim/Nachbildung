package redstonetim.nachbildung.gui

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import javafx.stage.Modality
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.gui.fxml.FXMLHandler
import redstonetim.nachbildung.io.IOHandler
import redstonetim.nachbildung.setting.Options
import redstonetim.nachbildung.setting.SettingsStage

/**
 * Controller for the main window.
 */
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
        IOHandler.openOldReconstructions()
    }

    @FXML
    private lateinit var tabPane: TabPane

    fun addReconstruction(reconstruction: ReconstructionNode) {
        reconstruction.update()
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
        val reconstruction = ReconstructionNode.create()
        SettingsStage.open(reconstruction.settings, onSave = {
            reconstruction.addSolve(SolveNode.create(reconstruction))
            addReconstruction(reconstruction)
            setSelectedReconstruction(reconstruction)
        })
    }

    @FXML
    private fun onMenuOpen(event: ActionEvent) {
        IOHandler.showOpenFilesDialog("Open", Main.stage, *extensionFilters)?.let { IOHandler.openReconstructions(it) }
    }

    @FXML
    fun onMenuSave(event: ActionEvent) {
        getSelectedReconstruction()?.let {
            IOHandler.saveReconstructionAs(it, it.fileLocation
                    ?: IOHandler.showSaveFileDialog("Save As...", Main.stage, it.getSuggestedFileName(), *extensionFilters))
        }
    }

    @FXML
    private fun onMenuSaveAs(event: ActionEvent) {
        getSelectedReconstruction()?.let {
            IOHandler.saveReconstructionAs(it, IOHandler.showSaveFileDialog("Save As...", Main.stage, it.getSuggestedFileName(), *extensionFilters))
        }
    }

    @FXML
    private fun onMenuSettings(event: ActionEvent) {
        Options.openSettingsDialog()
    }

    @FXML
    private fun onMenuSource(event: ActionEvent) = Main.instance.openLink("https://github.com/RedstoneTim/Nachbildung")

    @FXML
    private fun onMenuFeedback(event: ActionEvent) = Main.instance.openLink("https://github.com/RedstoneTim/Nachbildung/issues")

    @FXML
    private fun onMenuWiki(event: ActionEvent) = Main.instance.openLink("https://github.com/RedstoneTim/Nachbildung/wiki")

    @FXML
    private fun onMenuAbout(event: ActionEvent) {
        // TODO: FXML
        val stage = FXMLHandler.loadAboutStage()
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.title = Main.TITLE
        stage.showAndWait()
    }
}