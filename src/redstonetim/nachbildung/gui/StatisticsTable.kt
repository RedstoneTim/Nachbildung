package redstonetim.nachbildung.gui

import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.VBox
import redstonetim.nachbildung.step.StatisticsStep
import redstonetim.nachbildung.step.Step
import redstonetim.nachbildung.step.StepParser
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil

/**
 * A table for representing statistics of the solve/reconstruction.
 */
open class StatisticsTable(name: String = "") : VBox() {
    val table = TableView<StatisticsStep>()
    var name: String
        get() = label.text
        protected set(value) {
            label.text = value
        }
    private var label: Label = Label()
    private val nameColumn = TableColumn<StatisticsStep, String>("Step")
    private val timeColumn = TableColumn<StatisticsStep, String>("Time")
    private val stmColumn = TableColumn<StatisticsStep, String>("STM")
    private val stpsColumn = TableColumn<StatisticsStep, String>("STPS")
    private val etmColumn = TableColumn<StatisticsStep, String>("ETM")
    private val etpsColumn = TableColumn<StatisticsStep, String>("ETPS")
    private val steps = FXCollections.observableArrayList<StatisticsStep>()

    init {
        nameColumn.cellValueFactory = PropertyValueFactory("name")
        timeColumn.cellValueFactory = PropertyValueFactory("timeAsString")
        stmColumn.cellValueFactory = PropertyValueFactory("movesSTMAsString")
        stpsColumn.cellValueFactory = PropertyValueFactory("STPSAsString")
        etmColumn.cellValueFactory = PropertyValueFactory("movesETMAsString")
        etpsColumn.cellValueFactory = PropertyValueFactory("ETPSAsString")
        nameColumn.minWidth = 100.0
        timeColumn.minWidth = 50.0
        stmColumn.minWidth = 50.0
        stpsColumn.minWidth = 50.0
        etmColumn.minWidth = 50.0
        etpsColumn.minWidth = 50.0
        table.items = steps
        table.prefHeight = 170.0
        table.columns.addAll(nameColumn, timeColumn, stmColumn, stpsColumn, etmColumn, etpsColumn)
        label.text = name
        this.children.addAll(label, table)
    }

    open fun update(steps: List<StatisticsStep>) {
        this.steps.clear()
        this.steps.addAll(steps)
    }
}

abstract class ReconstructionStatisticsTable(name: String = "") : StatisticsTable(name) {
    init {
        managedProperty().bind(visibleProperty())
    }

    fun updateFromReconstruction(reconstructionNode: ReconstructionNode) {
        isVisible = update(reconstructionNode)
    }

    protected abstract fun update(reconstructionNode: ReconstructionNode): Boolean
}

class AverageStatisticsTable : ReconstructionStatisticsTable() {
    override fun update(reconstructionNode: ReconstructionNode): Boolean {
        val solves = reconstructionNode.solves
        val solveCount = solves.size
        return if (solveCount >= 5) {
            val removedSolves = ceil(solveCount * 0.05).toInt()
            name = "Average (${solveCount - (removedSolves * 2)}/$solveCount)"
            update(StatisticsStep.getMeanSteps(solves.stream()
                    .sorted().skip(removedSolves.toLong()).sorted(Collections.reverseOrder()).skip(removedSolves.toLong())
                    .map { StepParser.getStatisticsSteps(it.getSteps(), reconstructionNode.methodSetting.value) }.collect(Collectors.toList()), reconstructionNode.methodSetting.value.getStepNames()))
            true
        } else {
            false
        }
    }
}

class MeanStatisticsTable : ReconstructionStatisticsTable() {
    override fun update(reconstructionNode: ReconstructionNode): Boolean {
        val solves = reconstructionNode.solves
        val solveCount = solves.size
        return if (solveCount > 2) {
            name = "Mean ($solveCount/$solveCount)"
            update(StatisticsStep.getMeanSteps(solves.map { StepParser.getStatisticsSteps(it.getSteps(), reconstructionNode.methodSetting.value) },
                    reconstructionNode.methodSetting.value.getStepNames()))
            true
        } else {
            false
        }
    }
}

class BestFromFieldsStatisticsTable : ReconstructionStatisticsTable("Best from each field") {
    override fun update(reconstructionNode: ReconstructionNode): Boolean {
        val solves = reconstructionNode.solves
        val solveCount = solves.size
        return if (solveCount > 2) {
            val steps = solves.map { StepParser.getStatisticsSteps(it.getSteps(), reconstructionNode.methodSetting.value) }
            val bestSteps = arrayListOf<StatisticsStep>()
            for (stepName in Collections.singleton(Step.TOTAL) union reconstructionNode.methodSetting.value.getStepNames()) {
                var bestStep: StatisticsStep? = null
                for (stepList in steps) {
                    for (step in stepList) {
                        if ((step.name == stepName) && ((bestStep == null) || (bestStep.time > step.time))) {
                            bestStep = step
                        }
                    }
                }
                if (bestStep != null) {
                    bestSteps.add(bestStep)
                }
            }
            update(bestSteps)
            true
        } else {
            false
        }
    }
}