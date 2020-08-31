package redstonetim.nachbildung.step

import redstonetim.nachbildung.puzzle.Puzzle
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.floor
import kotlin.math.round

/**
 * This class represents steps of solving a [Puzzle], which are used for organizing the input and serving as the data for statistics.
 */
open class Step(val name: String, stepTime: Double, val movesSTM: Int, val movesETM: Int, val moves: List<Puzzle.Move>, vararg val statsToAddTo: String) {
    companion object {
        const val TOTAL = "Total"
        const val MOVE_NAME_SEPARATOR = "//"
        private val TIME_REGEX = Regex("(((\\d*):)?([0-5]?\\d):)?(\\d+)((\\.\\d{0,2})\\d*)?")
        private val DNF_REGEX = Regex("^D(NF?)?\$")
        private val standardDecimalFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
        private val bigDecimalFormat = DecimalFormat("00.00", DecimalFormatSymbols(Locale.US))

        init {
            standardDecimalFormat.roundingMode = RoundingMode.HALF_UP
            bigDecimalFormat.roundingMode = RoundingMode.HALF_UP
        }

        fun isValidTime(time: String, allowDNF: Boolean = true): Boolean =
                (time.trim().matches(DNF_REGEX) && allowDNF)
                        || time.trim().matches(TIME_REGEX)

        fun parseTime(time: String): Double {
            val matchResult = TIME_REGEX.matchEntire(time.trim())
            return when {
                (matchResult != null) -> {
                    val hours = matchResult.groups[3]?.value?.toIntOrNull() ?: 0
                    val minutes = matchResult.groups[4]?.value?.toIntOrNull() ?: 0
                    val seconds = matchResult.groups[5]?.value?.toIntOrNull() ?: 0
                    val millis = matchResult.groups[7]?.value?.toDoubleOrNull() ?: 0.0
                    (((hours * 60.0) + minutes) * 60.0) + seconds + millis
                }
                time.trim().matches(DNF_REGEX) -> Double.POSITIVE_INFINITY
                else -> 0.0
            }
        }

        fun timeToString(time: Double): String {
            return if (time == Double.POSITIVE_INFINITY) {
                "DNF"
            } else {
                val minutes = floor(time / 60.0)
                val seconds = time - (minutes * 60)
                if (minutes > 0) "${minutes.toInt()}:${bigDecimalFormat.format(seconds)}" else standardDecimalFormat.format(seconds)
            }
        }
    }

    constructor(moves: List<Puzzle.Move>, time: Double, name: String, puzzle: Puzzle, vararg statsToAddTo: String)
            : this(name, time, puzzle.moveManager.calculateMovecountSTM(moves), puzzle.moveManager.calculateMovecountETM(moves), moves, *statsToAddTo)

    val time = round(stepTime * 100.0) / 100.0 // round time so we don't end up with incorrect statistics
    open val stps: Double = if (time <= 0) 0.0 else round((movesSTM.toDouble() / time) * 100.0) / 100.0
    open val etps: Double = if (time <= 0) 0.0 else round((movesETM.toDouble() / time) * 100.0) / 100.0

    // use these for statistics so numbers are shown as 1.00 instead of 1 or 1.0000
    open fun getTimeAsString(): String = timeToString(time)

    open fun getMovesSTMAsString(): String = movesSTM.toString()

    open fun getMovesETMAsString(): String = movesETM.toString()

    open fun getSTPSAsString(): String = standardDecimalFormat.format(stps)

    open fun getETPSAsString(): String = standardDecimalFormat.format(etps)

    open fun getMovesAsString(): String = moves.joinToString(" ")

    open fun getComment(): String = "$MOVE_NAME_SEPARATOR $name"

    override fun toString(): String = "${getMovesAsString()} ${getComment()}".trim()
}