package redstonetim.nachbildung.puzzle.nxnxn

import javafx.scene.web.WebView
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.puzzle.Puzzle
import java.net.URI

/**
 * A [Puzzle] in the form of a cube with [n] layers, e.g. the Rubik's Cube (3x3x3) or the Rubik's Revenge (4x4x4).
 */
open class PuzzleNxNxN(val n: Int) : Puzzle("${n}x${n}x${n}") {
    override val moveManager: MoveManager = MoveManagerNxNxN(n)

    override fun getPuzzleVisualization(): PuzzleVisualization = PuzzleNxNxNVisualization(n)

    override fun getReconstructionLink(solve: SolveNode): String {
        // TODO: Fix link (better encoding and replace - with %26%2345%3B)
        val link = URI.create("https://alg.cubing.net/?type=reconstruction&view=playback&puzzle=")
        val query = "${link.query}${n}x${n}x${n}&title=Solve ${solve.getSolveNumber()} - ${solve.getTimeAsString()}&setup=${
            solve.getScrambleMoves().joinToString(separator = " ") {
                it.toString().replace('\'', '-')
            }
        }&alg=${
            solve.getSteps().stream().map { it.toString().replace('\'', '-') }.reduce { s1, s2 -> "$s1\n$s2" }.orElse("")
        }"
        return URI(link.scheme, link.authority, link.path, query, link.fragment).toString().replace("+", "%26%232b%3B")
    }

    /**
     * [Puzzle.PuzzleVisualization] for [Puzzle]s of type [PuzzleNxNxN] ranging from n=1 to n=10.
     * Uses a webview to get an image from VisualCube.
     */
    class PuzzleNxNxNVisualization(val n: Int) : PuzzleVisualization {
        override val node = WebView()
        private val link = URI.create("https://api.cubing.net/visualcube/visualcube.php?fmt=svg&pzl=$n&r=y20x-34&view=trans&size=230&alg=x2")
        private var lastQuery = link.toString()

        init {
            node.maxWidth = 300.0
            node.maxHeight = 300.0
        }

        override fun update(scrambleMoves: List<Puzzle.Move>, solution: String) {
            val query = "${this.link.query} ${scrambleMoves.joinToString()} $solution"
            if (query != lastQuery) {
                node.engine.load(URI(this.link.scheme, this.link.authority,
                        this.link.path, query, this.link.fragment).toString())
                lastQuery = query
            }
        }

        override fun representsPuzzle(puzzle: Puzzle): Boolean = (puzzle is PuzzleNxNxN) && (puzzle.n == n)
    }

}