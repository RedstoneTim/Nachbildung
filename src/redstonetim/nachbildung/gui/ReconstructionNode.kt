package redstonetim.nachbildung.gui

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
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
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.export.Exporter
import redstonetim.nachbildung.gui.fxml.FXMLHandler
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.method.Method
import redstonetim.nachbildung.puzzle.Puzzle
import redstonetim.nachbildung.setting.Options
import redstonetim.nachbildung.setting.Setting
import redstonetim.nachbildung.setting.SettingsStage
import redstonetim.nachbildung.setting.TimeInputType
import java.io.File
import java.util.*

/**
 * A [Node] representing a reconstruction, used both for GUI and the actual data.
 */
class ReconstructionNode : Tab(), JSONSerializable<ReconstructionNode> {
    companion object {
        fun create(): ReconstructionNode {
            val loaded = FXMLHandler.loadReconstruction()
            val reconstruction = loaded.second
            reconstruction.content = loaded.first
            reconstruction.update()
            return reconstruction
        }
    }

    // General
    private val titleProperty = SimpleStringProperty()
    internal var fileLocation: File? = null
    internal val savedProperty = SimpleBooleanProperty(false)

    // Solves
    @FXML
    private lateinit var solvesBox: VBox

    // not the nicest way to do this but there isn't really anything better
    private val solvesInternal = arrayListOf<SolveNode>()
    val solves: List<SolveNode> = Collections.unmodifiableList(solvesInternal)

    fun addSolve(solve: SolveNode): Boolean = if (solvesBox.children.add(solve)) {
        solvesInternal.add(solve)
        update()
        true
    } else {
        false
    }

    fun removeSolve(solve: SolveNode): Boolean = if (solvesBox.children.remove(solve)) {
        solvesInternal.remove(solve)
        update()
        true
    } else {
        false
    }

    // Settings
    val solverSetting = Setting.StringSetting("Solver", "", "solver")
    val detailsSetting = Setting.StringSetting("Details", "Ao5", "details")
    val competitionSetting = Setting.StringSetting("Competition", "Unofficial", "competition")
    val videoSetting = Setting.StringSetting("Video link", "", "video_link")
    val puzzleSetting = Setting.ChoiceSetting("Puzzle", Options.defaultPuzzle.value, Puzzle.values, "puzzle")
    val methodSetting = Setting.ChoiceSetting("Method", Options.defaultMethod.value, Method.values, "method")
    val fpsSetting = Setting.DoubleSetting("FPS", Options.defaultFPS.value, "fps")
    val timeInputTypeSetting = Setting.ChoiceSetting("Time input type",
            Options.defaultTimeSetting.value, TimeInputType.values().asList(), "time_input_type")
    val solutionSeparatorSetting = Setting.StringSetting("Solution separator", Options.defaultSolutonSeparator.value, "solution_separator")
    val autoFillStepsSetting = Setting.BooleanSetting("Auto fill steps (W.I.P.)", Options.defaultAutoFillStepsSetting.value, "auto_fill_steps")
    val settings = arrayListOf<Setting<*, *>>(solverSetting, detailsSetting, competitionSetting, videoSetting, puzzleSetting,
            methodSetting, fpsSetting, timeInputTypeSetting, solutionSeparatorSetting, autoFillStepsSetting)

    // Statistics
    @FXML
    private lateinit var statisticsBox: VBox

    init {
        setOnCloseRequest { showSavePopup(it) }
        titleProperty.bind(solverSetting.concat(" - ").concat(detailsSetting).concat(" - ").concat(competitionSetting))
        textProperty().bind(Bindings.`when`(savedProperty).then("").otherwise("*")
                .concat(titleProperty))
    }

    fun getStatistics(includeInvisible: Boolean = false): List<ReconstructionStatisticsTable> =
            statisticsBox.children.mapNotNull { if ((it is ReconstructionStatisticsTable) && (includeInvisible || it.isVisible)) it else null }

    @FXML
    private fun onSettingsButton(event: ActionEvent) {
        SettingsStage.open(settings, onSave = { update() })
    }

    @FXML
    private fun onExportReconstructionButton(event: ActionEvent) {
        Exporter.exportReconstruction(this)
    }

    @FXML
    private fun onAddSolveButton(event: ActionEvent) {
        addSolve(SolveNode.create(this))
    }

    override fun toString(): String = titleProperty.value

    fun getSuggestedFileName(): String = fileLocation?.nameWithoutExtension ?: toString()

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.`object`()
                .key("solves").array()
        for (solve in solves) {
            solve.toJSON(jsonWriter)
        }
        jsonWriter.endArray().key("setting").`object`()
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
        jsonObject.optJSONObject("setting")?.also {
            for (setting in settings) {
                setting.fromJSON(it)
            }
        }
        return this
    }

    fun showSavePopup(event: Event): Boolean {
        return if (savedProperty.value) {
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
            for (solve in solves) {
                solve.update(false)
            }
        }
        for (statisticsTable in getStatistics(true)) {
            statisticsTable.updateFromReconstruction(this)
        }
        savedProperty.value = false
    }
}