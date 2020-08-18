package redstonetim.nachbildung.puzzle

import javafx.scene.Group
import javafx.scene.Node
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.SolveNode

abstract class Puzzle(val name: String) : JSONSerializable<Puzzle> {
    companion object : LinkedHashMap<String, Puzzle>() {
        // TODO: Use options for default puzzle
        fun getDefaultPuzzle(): Puzzle = Puzzle["3x3x3"]!!
        val emptyPuzzleVisualization: PuzzleVisualization = object : Group(), PuzzleVisualization {
            override val node: javafx.scene.Node
                get() = this
            override fun update(scramble: String, solution: String) {}
            override fun representsPuzzle(puzzle: Puzzle) = true
        }

        init {
            for (n in 2..10) {
                PuzzleNxNxN(n).register()
            }
        }
    }

    fun register() {
        Puzzle[name] = this
    }

    override fun toString(): String = name

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("puzzle").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): Puzzle {
        return Puzzle[jsonObject.optString("puzzle")] ?: getDefaultPuzzle()
    }

    open fun getPuzzleVisualization(): PuzzleVisualization = emptyPuzzleVisualization

    abstract fun getReconstructionLink(solve: SolveNode): String

    abstract fun offsetMoves(moveString: String, offset: String): String

    // TODO: Improve movecount calculation
    abstract fun calculateMovecountSTM(moves: String): Int

    abstract fun calculateMovecountETM(moves: String): Int

    interface PuzzleVisualization {
        val node: Node

        fun update(scramble: String, solution: String)

        fun representsPuzzle(puzzle: Puzzle): Boolean
    }
}