package redstonetim.nachbildung.step

import redstonetim.nachbildung.puzzle.Puzzle
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * A special [Step] that cannot be used by [redstonetim.nachbildung.method.Method]s for statistics purposes.
 */
open class SpecialStep(name: String, stepTime: Double, movesSTM: Int, movesETM: Int, moves: List<Puzzle.Move>) :
        Step(name, stepTime, movesSTM, movesETM, moves) {
    companion object {
        val specialSteps = listOf(OffsetStep, InspectionStep, PenaltyStep, CommentStep)
    }

    interface SpecialStepManager<T : SpecialStep> {
        /**
         * Returns true if this step roughly matches, meaning that if it does but cannot be parsed, that line in the solution will be shown as error.
         */
        fun roughlyMatches(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): Boolean

        /**
         * Tries to create an instance. If not possible, null is returned. Should only be called when [roughlyMatches] returns true.
         */
        fun tryCreateInstance(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): T?
    }

    class OffsetStep(moves: List<Puzzle.Move>) : SpecialStep(OFFSET, 0.0, 0, 0, moves) {
        companion object : SpecialStepManager<OffsetStep> {
            const val OFFSET = "Offset"

            override fun roughlyMatches(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): Boolean = name == OFFSET

            override fun tryCreateInstance(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): OffsetStep? =
                    if ((rawTime == 0.0) && moves.isNotEmpty() && moves.all { it.isRotation }) OffsetStep(moves)
                    else null
        }
    }

    class InspectionStep(moves: List<Puzzle.Move>) : SpecialStep(INSPECTION, 0.0, 0, 0, moves) {
        companion object : SpecialStepManager<InspectionStep> {
            const val INSPECTION = "Inspection"

            override fun roughlyMatches(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): Boolean = name == INSPECTION

            override fun tryCreateInstance(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): InspectionStep? =
                    if ((rawTime == 0.0) && moves.all { it.isRotation }) InspectionStep(moves)
                    else null
        }
    }

    class PenaltyStep(timePenalty: Double) : SpecialStep(PENALTY, timePenalty, 0, 0, emptyList()) {
        companion object : SpecialStepManager<PenaltyStep> {
            const val PENALTY = "Penalty"
            private val plusTwoDecimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

            init {
                plusTwoDecimalFormat.roundingMode = RoundingMode.HALF_UP
            }

            override fun roughlyMatches(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): Boolean = name == PENALTY

            override fun tryCreateInstance(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): PenaltyStep? =
                    if (moves.isEmpty() && (rawTime > 0.0)) PenaltyStep(rawTime)
                    else null

            fun timePenaltyAsString(time: Double): String =
                    if (time == Double.POSITIVE_INFINITY) "DNF"
                    else "+${plusTwoDecimalFormat.format(time)}"
        }

        override fun getTimeAsString(): String = timePenaltyAsString(time)

        override fun getMovesAsString(): String = ""

        override fun getComment(): String = "$MOVE_NAME_SEPARATOR ${getTimeAsString()} Penalty"
    }

    class CommentStep(name: String) : SpecialStep(name, 0.0, 0, 0, emptyList()) {
        companion object : SpecialStepManager<CommentStep> {
            override fun roughlyMatches(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): Boolean = moves.isEmpty()

            override fun tryCreateInstance(name: String, rawTime: Double, actualTime: Double, moves: List<Puzzle.Move>): CommentStep? =
                    if (rawTime == 0.0) CommentStep(name)
                    else null
        }
    }
}