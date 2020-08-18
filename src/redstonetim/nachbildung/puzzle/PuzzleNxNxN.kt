package redstonetim.nachbildung.puzzle

import javafx.scene.web.WebView
import redstonetim.nachbildung.SolveNode
import java.net.URI

open class PuzzleNxNxN(val n: Int) : Puzzle("${n}x${n}x${n}") {
    companion object {
        private val MOVE_REGEX = Regex("^((\\d+-\\d+|\\d*)[RUFLDB]w|[RUFLDBMESmesxyz]|\\d*[RUFLDB]|(\\d+-\\d+|\\d*)[rufldb])(\\d*)('?)")
        private val MOVE_PARENTHESES_REGEX = Regex("^(\\([^()]+\\)|((\\d+-\\d+|\\d*)[RUFLDB]w|[RUFLDBMESmesxyz]|\\d*[RUFLDB]|(\\d+-\\d+|\\d*)[rufldb])(\\d*)('?))")
        private val PARENTHESIS_REGEX = Regex("[()]")
        private val COMMENT_REGEX = Regex("//.*$")
        private val rotations = arrayListOf("x", "y", "z")
    }

    fun parseMoves(moveString: String, ignoreParentheses: Boolean): ArrayList<String> {
        val moveList = arrayListOf<String>()
        var moves = (if (ignoreParentheses) moveString.replace(PARENTHESIS_REGEX, "") else moveString).replace(COMMENT_REGEX, "").trim()
        val regex = if (ignoreParentheses) MOVE_REGEX else MOVE_PARENTHESES_REGEX
        var matchResult = regex.find(moves)
        while (matchResult != null) {
            moveList.add(matchResult.value)
            moves = moves.substring(matchResult.range.last + 1).trim()
            matchResult = regex.find(moves)
        }
        // TODO: Show errors in notation somehow instead of ignoring?
        return moveList
    }

    fun getMoveType(move: String): String? = MOVE_REGEX.matchEntire(move)?.groups?.get(1)?.value

    fun getMovePower(move: String): Int {
        val groups = MOVE_REGEX.matchEntire(move)?.groups
        return (groups?.get(4)?.value?.toIntOrNull()?:1) * (if (groups?.get(5)?.value?.isNotBlank() == true) -1 else 1)
    }

    override fun getPuzzleVisualization(): PuzzleVisualization = PuzzleNxNxNVisualization()

    override fun getReconstructionLink(solve: SolveNode): String {
        val link = URI.create("https://alg.cubing.net/?type=reconstruction&view=playback&puzzle=")
        val query = "${link.query}${n}x${n}x${n}&title=Solve ${solve.getSolveNumber()} - ${solve.getTimeAsString()}&setup=${solve.getScrambleMoves()}&alg=${
        solve.getSteps().stream().map { it.toReconstructionString() }.reduce { s1, s2 -> "$s1\n$s2" }.orElse("")
        }"
        return URI(link.scheme, link.authority, link.path, query, link.fragment).toString().replace("+", "%26%232b%3B") // TODO: Why does this not convert the plus sign correctly?
    }

    override fun offsetMoves(moveString: String, offset: String): String {
        parseMoves(offset, true)
        // TODO: Finish
        return moveString
    }

    private fun reducePower(power: Int): Int = (power - (power / 4 - 1) * 4) % 4 // TODO: Can you improve on this?

    override fun calculateMovecountSTM(moves: String): Int {
        var count = 0
        for (move in parseMoves(moves, true)) {
            if (!rotations.contains(getMoveType(move))) {
                count++
            }
        }
        return count
    }

    override fun calculateMovecountETM(moves: String): Int = parseMoves(moves, false).size

    // TODO: Implement better version at some point
    inner class PuzzleNxNxNVisualization : PuzzleVisualization {
        override val node = WebView()
        private val link = URI.create("http://cube.rider.biz/visualcube.php?fmt=svg&pzl=$n&r=y20x-34&view=trans&size=300&alg=x2")
        private var lastQuery = link.toString()

        init {
            node.maxWidth = 300.0
            node.maxHeight = 300.0
        }

        override fun update(scramble: String, solution: String) {
            val query = "${this.link.query} $scramble $solution"
            if (query != lastQuery) {
                node.engine.load(URI(this.link.scheme, this.link.authority,
                        this.link.path, query, this.link.fragment).toString())
                lastQuery = query
            }
        }

        override fun representsPuzzle(puzzle: Puzzle): Boolean = (puzzle == this@PuzzleNxNxN)
    }
}