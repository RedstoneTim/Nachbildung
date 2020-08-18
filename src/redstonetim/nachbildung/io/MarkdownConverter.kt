package redstonetim.nachbildung.io

import javafx.stage.FileChooser
import redstonetim.nachbildung.ReconstructionNode
import redstonetim.nachbildung.SolveNode
import redstonetim.nachbildung.gui.StatisticsTable
import java.net.URI

object MarkdownConverter: Converter {
    private val TABLE_REGEX = Regex("[^|]")

    override val fileEndings: Array<FileChooser.ExtensionFilter> = arrayOf(FileChooser.ExtensionFilter("Markdown files", "*.md", "*.markdown"),
            FileChooser.ExtensionFilter("All files", "*.*"))

    override fun processReconstruction(reconstruction: ReconstructionNode): String =
    buildString {
        append("# ").append(reconstruction.titleSetting.value).append("\n\n")
        if (reconstruction.videoSetting.value.isNotBlank()) {
            append("[Link to video](").append(reconstruction.videoSetting.value).append(")\n\n")
        }
        for (solve in reconstruction.getSolves()) {
            append(processSolve(solve, false)).append("\n\n")
        }

        val statisticsList = reconstruction.getStatistics()
        if (statisticsList.isNotEmpty()) {
            append("---\n\n## Statistics\n\n")

            for (statistics in statisticsList) {
                append(processStatistics(statistics)).append("\n\n")
            }
        }
    }

    override fun processSolve(solve: SolveNode): String = processSolve(solve, true)

    private fun processSolve(solve: SolveNode, standalone: Boolean): String = buildString {
        if (standalone) {
            append("# ").append(solve.reconstruction.titleSetting.value).append("\n\n")
            if (solve.reconstruction.videoSetting.value.isNotBlank()) {
                append("[Link to video](").append(solve.reconstruction.videoSetting.value).append(")\n\n")
            }
        } else {
            append("### Solve ").append(solve.getSolveNumber()).append("\n\n")
        }
        append("```\n").append(solve.getScrambleMoves()).append("\n\n")
        solve.getSteps().forEach { append(it.toReconstructionString()).append("\n") }
        val reconstructionLink = solve.getReconstructionLink()
        append("```\n\nView at [").append(URI.create(reconstructionLink).host).append("](").append(reconstructionLink).append(")\n\n")
        append(processStatistics(solve.statisticsTable))
    }

    private fun processStatistics(statistics: StatisticsTable): String = buildString {
        val label = statistics.name
        if (label.isNotEmpty()) {
            append("### ").append(label).append("\n\n")
        }
        val top = statistics.table.columns.joinToString("|") { it.text }
        append(top).append("\n").append(top.replace(TABLE_REGEX, "-"))
        for (statisticsStep in statistics.table.items) {
            append("\n").append(listOf(statisticsStep.name, statisticsStep.getTimeAsString(), statisticsStep.getMovesSTMAsString(),
                    statisticsStep.getSTPSAsString(), statisticsStep.getMovesSTMAsString(), statisticsStep.getETPSAsString()).joinToString("|"))
        }
    }

    override fun toString(): String = "Markdown"
}