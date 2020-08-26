package redstonetim.nachbildung.puzzle.nxnxn

import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.puzzle.Puzzle
import java.net.URI

open class PuzzleNxNxN(val n: Int) : Puzzle("${n}x${n}x${n}") {
    override val moveManager: MoveManager = MoveManagerNxNxN(n)

    override fun getPuzzleVisualization(): PuzzleVisualization = PuzzleNxNxNVisualization(n)

    override fun getReconstructionLink(solve: SolveNode): String {
        val link = URI.create("https://alg.cubing.net/?type=reconstruction&view=playback&puzzle=")
        val query = "${link.query}${n}x${n}x${n}&title=Solve ${solve.getSolveNumber()} - ${solve.getTimeAsString()}&setup=${solve.getScrambleMoves().joinToString()}&alg=${
            solve.getSteps().stream().map { it.toString() }.reduce { s1, s2 -> "$s1\n$s2" }.orElse("")
        }"
        return URI(link.scheme, link.authority, link.path, query, link.fragment).toString().replace("+", "%26%232b%3B")
    }
}