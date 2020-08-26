package redstonetim.nachbildung.puzzle

import javafx.scene.Group
import javafx.scene.Node
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.nxnxn.PuzzleNxNxN

abstract class Puzzle(val name: String) : JSONSerializable<Puzzle> {
    companion object : LinkedHashMap<String, Puzzle>() {
        val emptyPuzzleVisualization: PuzzleVisualization = object : Group(), PuzzleVisualization {
            override val node: javafx.scene.Node
                get() = this

            override fun update(scrambleMoves: List<Move>, solution: String) {}
            override fun representsPuzzle(puzzle: Puzzle) = true
        }
        val puzzle3x3x3 = PuzzleNxNxN(3)

        init {
            puzzle3x3x3.register()
            PuzzleNxNxN(2).register()
            for (n in 4..10) {
                PuzzleNxNxN(n).register()
            }
        }
    }

    abstract val moveManager: MoveManager

    fun register() {
        Puzzle[name] = this
    }

    override fun toString(): String = name

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("puzzle").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): Puzzle {
        return Puzzle.getOrDefault(jsonObject.optString("puzzle"), puzzle3x3x3)
    }

    open fun getPuzzleVisualization(): PuzzleVisualization = emptyPuzzleVisualization

    abstract fun getReconstructionLink(solve: SolveNode): String

    interface MoveManager {
        fun calculateMovecountSTM(moves: List<Move>?): Int = moves?.filter { !it.isRotation && !it.isParenthesis && !it.isPause }?.size
                ?: 0

        fun calculateMovecountETM(moves: List<Move>?): Int {
            return if (moves?.isEmpty() == false) {
                var movecount = 0
                var parenthesisOpened = false
                for (move in moves) {
                    if (move.isPause) continue // don't include pauses

                    if (parenthesisOpened) {
                        if (move == Move.closingParenthesisMove) {
                            parenthesisOpened = false
                        }
                    } else if (move == Move.openingParenthesisMove) {
                        parenthesisOpened = true
                        movecount++
                    } else if (move != Move.closingParenthesisMove) { // this shouldn't happen but we'll check for it anyways
                        movecount++
                    }
                }
                movecount
            } else 0
        }

        fun parseMove(singleMoveString: String): Move?

        fun parseMoves(moveString: String): List<Move>?

        fun offsetMoves(moves: List<Move>, offsetRotations: List<Move>): List<Move>
    }

    interface Move {
        companion object {
            val openingParenthesisMove = object : Move {
                override val moveType = "("
                override val movePower = 0
                override val isRotation = false
                override val isParenthesis = true
                override fun toString(): String = moveType
            }
            val closingParenthesisMove = object : Move {
                override val moveType = ")"
                override val movePower = 0
                override val isRotation = false
                override val isParenthesis = true
                override fun toString(): String = moveType
            }
            val pauseMove = object : Move {
                override val moveType = "."
                override val movePower = 0
                override val isRotation = false
                override val isParenthesis = false
                override val isPause = true
                override fun toString(): String = moveType
            }
        }

        val moveType: String

        val movePower: Int

        val simplifiedMovePower: Int
            get() = movePower

        val isRotation: Boolean

        val isParenthesis: Boolean
            get() = false

        val isPause: Boolean
            get() = false
    }

    interface PuzzleVisualization {
        val node: Node

        fun update(scrambleMoves: List<Move>, solution: String)

        fun representsPuzzle(puzzle: Puzzle): Boolean
    }
}