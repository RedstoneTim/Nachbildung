package redstonetim.nachbildung.gui

import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.export.Exporter
import redstonetim.nachbildung.gui.fxml.FXMLHandler
import redstonetim.nachbildung.gui.textfield.TimeTextField
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.Puzzle
import redstonetim.nachbildung.puzzle.Step
import redstonetim.nachbildung.setting.Options

// TODO: Fix issue that all solves have to be reloaded
class SolveNode : Group(), JSONSerializable<SolveNode>, Comparable<SolveNode> {
    companion object {
        fun create(reconstruction: ReconstructionNode): SolveNode {
            val loaded = FXMLHandler.loadSolve()
            val solve = loaded.second
            solve.reconstruction = reconstruction
            solve.puzzleVisualization = reconstruction.puzzleSetting.value.getPuzzleVisualization()
            solve.puzzleVisualizationContainer.children.add(solve.puzzleVisualization.node)
            StackPane.setAlignment(solve.puzzleVisualization.node, Pos.CENTER)
            solve.children.add(loaded.first)
            solve.update()
            return solve
        }
    }

    @FXML
    fun initialize() {
        val changeListener = { _: ObservableValue<out String>, _: String, _: String ->
            update()
        }
        scrambleTextField.textProperty().addListener(changeListener)
        timeTextField.textProperty().addListener(changeListener)
        solutionTextArea.textProperty().addListener(changeListener)
        solutionTextArea.caretPositionProperty().addListener { _, _, _ -> updatePuzzleVisualization() }
    }

    lateinit var reconstruction: ReconstructionNode private set
    private lateinit var puzzleVisualization: Puzzle.PuzzleVisualization

    @FXML
    private lateinit var puzzleVisualizationContainer: StackPane

    @FXML
    private lateinit var scrambleTextField: TextField

    @FXML
    private lateinit var timeTextField: TimeTextField

    @FXML
    private lateinit var solutionTextArea: TextArea

    @FXML
    lateinit var statisticsTable: StatisticsTable

    @FXML
    private fun onOpenLinkButton(event: ActionEvent) {
        Main.instance.openLink(getReconstructionLink())
    }

    @FXML
    private fun onExportSolveButton(event: ActionEvent) {
        Exporter.exportSolve(this)
    }

    @FXML
    private fun onRemoveSolveButton(event: ActionEvent) {
        // TODO: FXML
        val remove = Stage()
        remove.initModality(Modality.APPLICATION_MODAL)
        val cancelButton = Button("Cancel")
        cancelButton.setOnAction { remove.close() }
        val removeButton = Button("Remove solve")
        removeButton.setOnAction {
            reconstruction.removeSolve(this)
            remove.close()
        }
        val buttons = HBox(cancelButton, removeButton)
        buttons.alignment = Pos.CENTER
        val content = VBox(Label("Remove the solve?"), buttons)
        remove.title = Main.TITLE
        remove.scene =
                Scene(content)
        remove.showAndWait()
    }

    override fun toString(): String = reconstruction.toString()

    fun getSuggestedFileName(): String = reconstruction.getSuggestedFileName()

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.`object`()
                .key("scramble").value(scrambleTextField.text)
                .key("solution").value(solutionTextArea.text)
                .key("time").value(timeTextField.textAsTime)
                .endObject()
    }

    override fun fromJSON(jsonObject: JSONObject): SolveNode {
        scrambleTextField.text = jsonObject.optString("scramble", "")!!
        solutionTextArea.text = jsonObject.optString("solution", "")!!
        timeTextField.textAsTime = jsonObject.optDouble("time", 0.0)
        update()
        return this
    }

    override fun compareTo(other: SolveNode) = getTime().compareTo(other.getTime())

    fun getTime(): Double = getSteps().stream().map { it.time }.reduce(0.0, Double::plus)

    fun getTimeAsString(): String = Step.timeToString(getTime())

    fun getSolveNumber(): Int = reconstruction.getSolves().indexOf(this) + 1

    fun getScrambleMoves(): String = scrambleTextField.text.trim()

    fun getSolutionMoves(untilCaret: Boolean): String {
        return (if (untilCaret) solutionTextArea.text.substring(0, solutionTextArea.caretPosition) else solutionTextArea.text)
                .split('\n').stream().map {
                    val start = it.indexOf(Options.solutionSeparator.value) + 1
                    if (start == 0) {
                        ""
                    } else {
                        val end = it.indexOf(Options.solutionSeparator.value, start)
                        it.substring(start, if (end >= 0) end else it.length).trim()
                    }
                }.filter(String::isNotBlank).reduce { s1, s2 -> "$s1 $s2" }.orElse("")
    }

    private fun getStepsOrNull(): List<Step>? = Step.parseStepsFromText(solutionTextArea.text, timeTextField.textAsTime, reconstruction.methodSetting.value, reconstruction.autoFillStepsSetting.value,
            scrambleTextField.text, reconstruction.puzzleSetting.value, reconstruction.fpsSetting.value, reconstruction.timeInputTypeSetting.value)

    /**
     * Returns a list of [Step]s parsed from the given solution input.
     */
    fun getSteps(): List<Step> = getStepsOrNull() ?: emptyList()

    fun getReconstructionLink(): String = reconstruction.puzzleSetting.value.getReconstructionLink(this)

    fun update(updateReconstruction: Boolean = true) {
        val steps = getStepsOrNull()
        // TODO: Make stuff red as well when the scramble sucks
        solutionTextArea.style = if (steps == null) {
            "-fx-text-box-border: red; -fx-focus-color: red; -fx-control-inner-background: rgba(255,0,0,0.1); -fx-text-fill: #000000;"
        } else {
            ""
        }
        statisticsTable.update(reconstruction.methodSetting.value.getStatisticsSteps(steps ?: emptyList()))
        if (!puzzleVisualization.representsPuzzle(reconstruction.puzzleSetting.value)) {
            puzzleVisualization = reconstruction.puzzleSetting.value.getPuzzleVisualization()
            puzzleVisualizationContainer.children.setAll(puzzleVisualization.node)
            StackPane.setAlignment(puzzleVisualization.node, Pos.CENTER)
        }
        if (updateReconstruction) {
            reconstruction.update(false)
        }
    }

    private fun updatePuzzleVisualization() {
        puzzleVisualization.update(getScrambleMoves(), getSolutionMoves(true))
    }
}