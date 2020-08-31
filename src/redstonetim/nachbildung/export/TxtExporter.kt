package redstonetim.nachbildung.export

import javafx.stage.FileChooser
import redstonetim.nachbildung.gui.ReconstructionNode
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.gui.StatisticsTable
import java.net.URI

/**
 * Exports a reconstruction or solve to a plain text format, without any markup.
 */
object TxtExporter : Exporter {
    const val INDENT = "     "
    override val fileEndings: Array<FileChooser.ExtensionFilter> = arrayOf(FileChooser.ExtensionFilter("Text files", "*.txt"),
            FileChooser.ExtensionFilter("All files", "*.*"))
    private val TABLE_REGEX = Regex("[^|]")

    override fun processReconstruction(reconstruction: ReconstructionNode): String =
            buildString {
                val title = reconstruction.toString()
                append(title).append("\n\n").append("-".repeat(title.length)).append("\n\n")
                if (reconstruction.videoSetting.value.isNotBlank()) {
                    append("Video: ").append(reconstruction.videoSetting.value).append("\n\n")
                }
                for (solve in reconstruction.solves) {
                    append(processSolve(solve, false)).append("\n\n\n")
                }

                val statisticsList = reconstruction.getStatistics()
                if (statisticsList.isNotEmpty()) {
                    append("\n\nStatistics\n\n")

                    for (statistics in statisticsList) {
                        append(processStatistics(statistics)).append("\n\n")
                    }
                }
            }

    override fun processSolve(solve: SolveNode): String = processSolve(solve, true)

    private fun processSolve(solve: SolveNode, standalone: Boolean): String = buildString {
        if (standalone) {
            val title = solve.toString()
            append(title).append("\n\n").append("-".repeat(title.length)).append("\n\n")
            if (solve.reconstruction.videoSetting.value.isNotBlank()) {
                append("Video: ").append(solve.reconstruction.videoSetting.value).append("\n\n")
            }
        } else {
            append("Solve ").append(solve.getSolveNumber()).append(": ").append(solve.getTimeAsString()).append("\n\n")
        }
        append(INDENT).append(solve.getScrambleMoves().joinToString(" ")).append("\n\n")
        solve.getSteps().forEach { append(INDENT).append(it.toString()).append("\n") }
        val reconstructionLink = solve.getReconstructionLink()
        append("\n\n").append(URI.create(reconstructionLink).host).append(": ").append(reconstructionLink).append("\n\n")
        append(processStatistics(solve.statisticsTable))
    }

    private fun processStatistics(statistics: StatisticsTable): String = buildString {
        val label = statistics.name
        if (label.isNotEmpty()) {
            append(label).append("\n\n")
        }
        val top = statistics.table.columns.map { it.text }
        val sizes = top.map { it.length }.toMutableList()
        val table = statistics.table.items.map {
            val list = listOf(it.name, it.getTimeAsString(), it.getMovesSTMAsString(),
                    it.getSTPSAsString(), it.getMovesETMAsString(), it.getETPSAsString())
            list.forEachIndexed { index, s -> sizes[index] = kotlin.math.max(sizes[index], s.length) }
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

    override fun toString(): String = "Plain text"
}