package redstonetim.nachbildung

import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.StatisticsTable
import redstonetim.nachbildung.gui.TimeTextField
import redstonetim.nachbildung.io.Converter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.Puzzle
import redstonetim.nachbildung.settings.Options

// TODO: Make statistics work again (worked like literally two minutes ago)
class SolveNode : Group(), JSONSerializable<SolveNode>, Comparable<SolveNode> {
    companion object {
        private val fxmlLocation = this::class.java.getResource("solve_node.fxml")

        fun create(reconstruction: ReconstructionNode): SolveNode {
            val loader = FXMLLoader()
            val content: Node = loader.load(fxmlLocation.openStream())
            val solve: SolveNode = loader.getController()
            solve.reconstruction = reconstruction
            solve.puzzleVisualization = reconstruction.puzzleSetting.value.getPuzzleVisualization()
            solve.puzzleVisualizationGroup.children.add(solve.puzzleVisualization.node)
            solve.children.add(content)
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
    private lateinit var puzzleVisualizationGroup: Group

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
        Converter.exportSolve(this)
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

    fun getScrambleMoves(): String = scrambleTextField.text

    fun getSolutionMoves(untilCaret: Boolean = false): String = (if (untilCaret) solutionTextArea.text.substring(0, solutionTextArea.caretPosition) else solutionTextArea.text)
            .split('\n').stream().map {
                val end = it.indexOf(Options.solutionSeparator.value)
                it.substring(0, if (end >= 0) end else it.length).trim()
            }.filter(String::isNotBlank).reduce { s1, s2 -> "$s1 $s2" }.orElse("")

    /**
     * Returns a list of [Step]s parsed from the given solution input.
     */
    fun getSteps(): List<Step> = Step.parseStepsFromText(solutionTextArea.text, timeTextField.textAsTime, reconstruction.methodSetting.value, reconstruction.autoFillStepsSetting.value,
            scrambleTextField.text, reconstruction.puzzleSetting.value, reconstruction.fpsSetting.value, reconstruction.timeInputTypeSetting.value)

    fun getReconstructionLink(): String = reconstruction.puzzleSetting.value.getReconstructionLink(this)

    fun update(updateReconstruction: Boolean = true) {
        statisticsTable.update(reconstruction.methodSetting.value.getStatisticsSteps(getSteps()))
        if (!puzzleVisualization.representsPuzzle(reconstruction.puzzleSetting.value)) {
            puzzleVisualization = reconstruction.puzzleSetting.value.getPuzzleVisualization()
            puzzleVisualizationGroup.children.setAll(puzzleVisualization.node)
        }
        if (updateReconstruction) {
            reconstruction.update(false)
        }
    }

    private fun updatePuzzleVisualization() {
        puzzleVisualization.update(getScrambleMoves(), getSolutionMoves(true))
    }
}