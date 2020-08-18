package redstonetim.nachbildung

import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.*
import redstonetim.nachbildung.io.Converter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.Method
import redstonetim.nachbildung.puzzle.Puzzle
import redstonetim.nachbildung.settings.Options
import redstonetim.nachbildung.settings.Setting
import redstonetim.nachbildung.settings.TimeInputType
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.properties.Delegates

// TODO: Statistics tables in FXML (not code)
class ReconstructionNode : Tab(), JSONSerializable<ReconstructionNode> {
    companion object {
        private val fxmlLocation = this::class.java.getResource("reconstruction_node.fxml")

        fun create(name: String): ReconstructionNode {
            val loader = FXMLLoader()
            val content: Node = loader.load(fxmlLocation.openStream())
            val reconstruction: ReconstructionNode = loader.getController()
            reconstruction.titleSetting.value = name
            reconstruction.content = content
            reconstruction.update()
            return reconstruction
        }
    }

    init {
        setOnCloseRequest { showSavePopup(it) }
    }

    // General
    internal var fileLocation: File? = null
    internal var saved = false
        set(value) {
            text = if (value) {
                titleSetting.value
            } else {
                "*${titleSetting.value}"
            }
            field = value
        }

    // Solves
    @FXML
    private lateinit var solvesBox: VBox // TODO: Add separators between solves?

    // TODO: Better performance (don't constantly create new collections)
    fun getSolves(): List<SolveNode> = solvesBox.children.mapNotNull { if (it is SolveNode) it else null }

    fun addSolve(solve: SolveNode): Boolean = if (solvesBox.children.add(solve)) {
        update()
        true
    } else {
        false
    }

    fun removeSolve(solve: SolveNode): Boolean = if (solvesBox.children.remove(solve)) {
        update()
        true
    } else {
        false
    }

    // Settings
    val titleSetting = object : Setting.StringSetting("Title", "Unnamed") {
        override fun saveChanges() {
            super.saveChanges()
        }
    }
    val videoSetting = Setting.StringSetting("Video", "")
    val puzzleSetting = object : Setting.ChoiceSetting<Puzzle>("Puzzle", Puzzle.getDefaultPuzzle(), Puzzle.values) {
        override fun saveChanges() {
            super.saveChanges()
            for (solve in getSolves())
                solve.update()
        }
    }
    val methodSetting = Setting.ChoiceSetting("Method", Method.getDefaultMethod(), Method.values)
    val fpsSetting = Setting.DoubleSetting("FPS", 60.0)
    val timeInputTypeSetting = object : Setting.ChoiceSetting<TimeInputType>("Time input type",
            Options.defaultTimeSetting.value, TimeInputType.values().asList()) {
        override fun saveChanges() {
            super.saveChanges()
            for (solve in getSolves())
                solve.update()
        }
    }
    val autoFillStepsSetting = Setting.BooleanSetting("Auto fill steps", false)
    val settings = arrayListOf<Setting<*, *>>(titleSetting, videoSetting, puzzleSetting, methodSetting, fpsSetting, timeInputTypeSetting, autoFillStepsSetting)

    // Statistics
    @FXML
    private lateinit var statisticsBox: VBox

    // TODO: Better performance (don't constantly create new collections)
    fun getStatistics(includeInvisible: Boolean = false): List<ReconstructionStatisticsTable> =
            statisticsBox.children.mapNotNull { if ((it is ReconstructionStatisticsTable) && (includeInvisible || it.isVisible)) it else null }

    @FXML
    private fun onSettingsButton(event: ActionEvent) {
        Setting.openSettingsStage(settings, onSave = { update() })
    }

    @FXML
    private fun onExportReconstructionButton(event: ActionEvent) {
        Converter.exportReconstruction(this)
    }

    @FXML
    private fun onAddSolveButton(event: ActionEvent) {
        addSolve(SolveNode.create(this))
    }

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.`object`()
                .key("solves").array()
        // TODO: Save stuff here
        for (solve in getSolves()) {
            solve.toJSON(jsonWriter)
        }
        jsonWriter.endArray().key("settings").`object`()
        for (setting in settings) {
            setting.toJSON(jsonWriter)
        }
        jsonWriter.endObject().endObject()
    }

    override fun fromJSON(jsonObject: JSONObject): ReconstructionNode {
        solvesBox.children.clear()
        jsonObject.optJSONArray("solves")?.also {
            for (jsonSolve in it) {
                if (jsonSolve is JSONObject) {
                    val solve = SolveNode.create(this)
                    addSolve(solve)
                    solve.fromJSON(jsonSolve)
                }
            }
        }
        jsonObject.optJSONObject("settings")?.also {
            for (setting in settings) {
                setting.fromJSON(it)
            }
        }
        return this
    }

    fun showSavePopup(event: Event): Boolean {
        // TODO: FXML
        return if (saved) {
            false
        } else {
            val cancelOrSave = Stage()
            cancelOrSave.initModality(Modality.APPLICATION_MODAL)
            val cancelButton = Button("Cancel")
            cancelButton.setOnAction {
                event.consume()
                cancelOrSave.close()
            }
            val dontSaveButton = Button("Don't save")
            dontSaveButton.setOnAction {
                cancelOrSave.close()
            }
            val buttons = HBox(cancelButton, dontSaveButton)
            buttons.alignment = Pos.CENTER
            val content = VBox(Label("Delete the performed changes?"), buttons)
            cancelOrSave.title = Main.TITLE
            cancelOrSave.scene =
                    Scene(content)
            cancelOrSave.showAndWait()
            true
        }
    }

    fun update(updateSolves: Boolean = true) {
        if (updateSolves) {
            for (solve in getSolves()) {
                solve.update(false)
            }
        }
        for (statisticsTable in getStatistics(true)) {
            statisticsTable.updateFromReconstruction(this)
        }
        saved = false
    }
}