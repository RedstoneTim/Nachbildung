package redstonetim.nachbildung.setting

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.stage.Modality
import javafx.stage.Stage
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.gui.fxml.FXMLHandler

class SettingsStage {
    companion object {
        fun open(settings: Collection<Setting<*, *>>, onSave: (ActionEvent) -> Unit = {}, onCancel: (ActionEvent) -> Unit = {}) {
            fun <T> updateNodeValue(setting: Setting<T, *>) = setting.setNodeValue(setting.value)

            val settingsStage = FXMLHandler.loadSettingsStage()
            settingsStage.onSave = onSave
            settingsStage.onCancel = onCancel
            settingsStage.settings = settings
            var index = -1
            val content = settingsStage.settingsGridPane
            settings.forEach {
                updateNodeValue(it)
                content.add(Label(it.displayName), 0, ++index)
                content.add(it.node, 1, index)
            }
            settingsStage.stage.title = Main.TITLE
            settingsStage.stage.showAndWait()
        }
    }

    private var onSave: (ActionEvent) -> Unit = {}
    private var onCancel: (ActionEvent) -> Unit = {}

    @FXML
    private lateinit var stage: Stage

    @FXML
    private lateinit var settingsGridPane: GridPane
    private lateinit var settings: Collection<Setting<*, *>>

    @FXML
    fun initialize() {
        stage.initModality(Modality.APPLICATION_MODAL)
    }

    @FXML
    fun onResetToDefaultButton(event: ActionEvent) {
        for (setting in settings) {
            resetNodeValue(setting)
        }
    }

    private fun <T> resetNodeValue(setting: Setting<T, *>) = setting.setNodeValue(setting.defaultValue)

    @FXML
    fun onSaveButton(event: ActionEvent) {
        settings.forEach { it.saveChanges() }
        onSave.invoke(event)
        stage.close()
    }

    @FXML
    fun onCancelButton(event: ActionEvent) {
        onCancel.invoke(event)
        stage.close()
    }
}