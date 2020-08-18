package redstonetim.nachbildung.io

import redstonetim.nachbildung.ReconstructionNode
import redstonetim.nachbildung.SolveNode
import redstonetim.nachbildung.gui.StatisticsTable
import java.net.URI

object BBCodeConverter : Converter {
    private val YOUTUBE_REGEX = Regex("^.*(?:(?:youtu\\.be/|v/|vi/|u/\\w/|embed/)|(?:(?:watch)?\\?v(?:i)?=|&v(?:i)?=))([^#&?]*).*")

    override fun processReconstruction(reconstruction: ReconstructionNode): String =
            buildString {
                append(reconstruction.titleSetting.value).append("\n\n")
                if (reconstruction.videoSetting.value.isNotBlank()) {
                    val videoLink = reconstruction.videoSetting.value
                    if (videoLink.isNotBlank()) {
                        append(processMedia(reconstruction.videoSetting.value)).append("\n")
                    }
                }
                for (solve in reconstruction.getSolves()) {
                    append(processSolve(solve, false)).append("\n")
                }

                val statisticsList = reconstruction.getStatistics()
                if (statisticsList.isNotEmpty()) {
                    append("[SPOILER=\"Statistics\"]\n")
                    for (statistics in statisticsList) {
                        append(processStatistics(statistics)).append("\n")
                    }
                    append("[/SPOILER]")
                }
            }

    override fun processSolve(solve: SolveNode): String = processSolve(solve, true)

    private fun processSolve(solve: SolveNode, standalone: Boolean): String = buildString {
        if (standalone) {
            append(solve.reconstruction.titleSetting.value).append("\n\n")
            val videoLink = solve.reconstruction.videoSetting.value
            if (videoLink.isNotBlank()) {
                append(processMedia(solve.reconstruction.videoSetting.value)).append("\n")
            }
        } else {
            append("[SPOILER=\"\nSolve ").append(solve.getSolveNumber()).append("\"]\n")
        }
        append(solve.getScrambleMoves()).append("\n\n")
        solve.getSteps().forEach {
            val reconstructionString = it.toReconstructionString()
            val stepStart = reconstructionString.indexOf('/') // TODO: Maybe set using let or something like that?
            if (stepStart < 0)
                append(reconstructionString)
            else
                append(reconstructionString.substring(0, stepStart)).append("[COLOR=rgb(128, 128, 128)]")
                        .append(reconstructionString.substring(stepStart)).append("[/COLOR]")
            append("\n")
        }
        val reconstructionLink = solve.getReconstructionLink()
        append("\n[COLOR=rgb(128, 128, 128)]View at [/COLOR][URL='").append(reconstructionLink).append("']")
                .append(URI.create(reconstructionLink).host).append("[/URL]\n")
        append(processStatistics(solve.statisticsTable))
        if (!standalone) append("[/SPOILER]")
    }

    private fun processStatistics(statistics: StatisticsTable): String = buildString {
        val label = statistics.name
        append("[SPOILER=\"").append(if (label.isEmpty()) "Statistics" else label).append("\"]\n[TABLE]\n[TR]\n")
        for(statisticsStep in statistics.table.columns) {
            append("[TD]").append(statisticsStep.text).append("[/TD]")
        }
        append("\n[/TR]")
        for (statisticsStep in statistics.table.items) {
            append("\n[TR]")
            for (name in listOf(statisticsStep.name, statisticsStep.getTimeAsString(), statisticsStep.getMovesSTMAsString(),
                    statisticsStep.getSTPSAsString(), statisticsStep.getMovesSTMAsString(), statisticsStep.getETPSAsString())) {
                append("[TD]").append(name).append("[/TD]")
            }
            append("\n[/TR]")
        }
        append("[/TABLE]\n[/SPOILER]")
    }

    private fun processMedia(mediaLink: String): String {
        val youtube = YOUTUBE_REGEX.find(mediaLink)
        return if (youtube == null) {
            "[URL='$mediaLink']Link to video[/URL]"
        } else {
            "[SPOILER=\"Video\"][MEDIA=youtube]${youtube.groupValues[1]}[/MEDIA][/SPOILER]"
        }
    }

    override fun toString(): String = "BBCode"
}