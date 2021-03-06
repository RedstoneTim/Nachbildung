package redstonetim.nachbildung.export

import javafx.stage.FileChooser
import redstonetim.nachbildung.gui.ReconstructionNode
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.gui.StatisticsTable
import java.net.URI
import kotlin.math.max

/**
 * Exports a reconstruction or solve to the Markdown format.
 */
object MarkdownExporter : Exporter {
    override val fileEndings: Array<FileChooser.ExtensionFilter> = arrayOf(FileChooser.ExtensionFilter("Markdown files", "*.md", "*.markdown"),
            FileChooser.ExtensionFilter("All files", "*.*"))
    private val TABLE_REGEX = Regex("[^|]")

    override fun processReconstruction(reconstruction: ReconstructionNode): String =
            buildString {
                append("# ").append(toString(reconstruction)).append("\n\n")
                if (reconstruction.videoSetting.value.isNotBlank()) {
                    append("[Link to video](").append(reconstruction.videoSetting.value).append(")\n\n")
                }
                for (solve in reconstruction.solves) {
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
            append("# ").append(toString(solve.reconstruction)).append("\n\n")
            if (solve.reconstruction.videoSetting.value.isNotBlank()) {
                append("[Link to video](").append(solve.reconstruction.videoSetting.value).append(")\n\n")
            }
        } else {
            append("### Solve ").append(solve.getSolveNumber()).append(": ").append(solve.getTimeAsString()).append("\n\n")
        }
        append("```\n").append(solve.getScrambleMoves().joinToString(" ")).append("\n\n")
        solve.getSteps().forEach { append(it.toString()).append("\n") }
        val reconstructionLink = solve.getReconstructionLink()
        append("```\n\nView at [").append(URI.create(reconstructionLink).host).append("](").append(reconstructionLink).append(")\n\n")
        append(processStatistics(solve.statisticsTable))
    }

    private fun processStatistics(statistics: StatisticsTable): String = buildString {
        val label = statistics.name
        if (label.isNotEmpty()) {
            append("### ").append(label).append("\n\n")
        }
        val top = statistics.table.columns.map { it.text }
        val sizes = top.map { it.length }.toMutableList()
        val table = statistics.table.items.map {
            val list = listOf(it.name, it.getTimeAsString(), it.getMovesSTMAsString(),
                    it.getSTPSAsString(), it.getMovesETMAsString(), it.getETPSAsString())
            list.forEachIndexed { index, s -> sizes[index] = max(sizes[index], s.length) }
            list
        }.map {
            it.mapIndexed { index, s -> " $s ${" ".repeat(sizes[index] - s.length)} " }
        }
        val firstRow = top.mapIndexed { index, s -> " $s ${" ".repeat(sizes[index] - s.length)} " }.joinToString("|")
        append(firstRow).append("\n").append(firstRow.replace(TABLE_REGEX, "-"))
        for (row in table) {
            append("\n").append(row.joinToString("|"))
        }
    }

    private fun toString(reconstruction: ReconstructionNode): String =
            "**${reconstruction.solverSetting.value}** - ${reconstruction.detailsSetting.value} - ${reconstruction.competitionSetting.value}"

    override fun toString(): String = "Markdown"
}