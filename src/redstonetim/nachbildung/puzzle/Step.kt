package redstonetim.nachbildung.puzzle

import redstonetim.nachbildung.setting.Options
import redstonetim.nachbildung.setting.TimeInputType
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.floor
import kotlin.math.round

/**
 * This class represents steps of solving the puzzle.
 * @param name The name of the step.
 * @param time The time in milliseconds needed to solve this step.
 * @param movesSTM The moves performed in this step in Slice Turn Metric.
 * @param movesETM The moves performed in this step in Execution Turn Metric.
 */
// TODO: Custom classes for special steps (inspection, penalty, offset etc.)
open class Step(val name: String, stepTime: Double, val movesSTM: Int, val movesETM: Int, val moves: List<Puzzle.Move>, vararg val statsToAddTo: String) {
    companion object {
        const val INSPECTION = "Inspection"
        const val TOTAL = "Total"
        const val PENALTY = "Penalty"
        const val OFFSET = "Offset"
        private val TIME_REGEX = Regex("(((\\d*):)?([0-5]?\\d):)?([0-5]?\\d)((\\.\\d{2})\\d*)?")
        private val DNF_REGEX = Regex("^DN*F*\$")
        private val standardDecimalFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
        private val bigDecimalFormat = DecimalFormat("00.00", DecimalFormatSymbols(Locale.US))
        private val plusTwoDecimalFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

        init {
            standardDecimalFormat.roundingMode = RoundingMode.HALF_UP
            bigDecimalFormat.roundingMode = RoundingMode.HALF_UP
            plusTwoDecimalFormat.roundingMode = RoundingMode.HALF_UP
        }

        fun isInspection(stepName: String, time: Double, moves: List<Puzzle.Move>): Boolean = (stepName == INSPECTION) && (time == 0.0) && moves.isNotEmpty() && moves.all { it.isRotation }

        fun isPenalty(stepName: String, noMoves: Boolean): Boolean = (stepName == PENALTY) && noMoves

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

        fun timeToString(time: Double, plusTwoPenalty: Boolean = false): String {
            return if (time == Double.POSITIVE_INFINITY) {
                "DNF"
            } else if (plusTwoPenalty) {
                "+${plusTwoDecimalFormat.format(time)}"
            } else {
                val minutes = floor(time / 60.0)
                val seconds = time - (minutes * 60)
                if (minutes > 0) "${minutes.toInt()}:${bigDecimalFormat.format(seconds)}" else standardDecimalFormat.format(seconds)
            }
        }

        /**
         * Parses a list of Steps from the given input
         */
        fun parseStepsFromText(solution: String, timeExclDNF: Double, method: Method, autoFillStep: Boolean, scramble: String, puzzle: Puzzle,
                               fps: Double, timeInputType: TimeInputType): List<Step>? {
            val performedMoves = arrayListOf<Puzzle.Move>()
            val offset = arrayListOf<Puzzle.Move>()
            val steps = solution.split('\n').mapNotNull { line ->
                val parts = line.split(Options.solutionSeparator.value).map { it.trim() }
                val moves = puzzle.moveManager.parseMoves(parts.getOrElse(1) { "" })?.let {
                    if (offset.isEmpty()) it else puzzle.moveManager.offsetMoves(it, offset)
                } ?: return@parseStepsFromText null
                val rawName = parts.getOrElse(2) { "" }

                // add offset
                if ((rawName == OFFSET) && moves.isNotEmpty()) {
                    if (moves.all { it.isRotation }) {
                        moves.let { offset.addAll(it) }
                    }
                    return@mapNotNull null
                }

                // add moves for auto-fill
                performedMoves.addAll(moves)

                // parse name
                val name = if (rawName.isBlank()) {
                    if (autoFillStep) method.getStepName(scramble, performedMoves.toString()) else return@mapNotNull null
                } else {
                    rawName
                }

                // TODO: Add some kind of error thing if the step doesn't exist

                // parse time
                val isPenalty = isPenalty(name, moves.isEmpty())
                val rawTime = parts.getOrNull(0)
                val time = if ((rawTime != null) && isValidTime(rawTime, isPenalty)) {
                    val parsedTime = parseTime(rawTime)
                    if ((timeInputType == TimeInputType.TOTAL_FRAMES) || (timeInputType == TimeInputType.START_FRAME)) parsedTime / fps
                    else parsedTime
                } else {
                    return@mapNotNull null
                }

                // you're not allowed to name anything Penalty or Inspection which isn't actually a penalty or inspection
                // Inspection is also not allowed to have no moves (though you can have that if you count with frames)
                if ((!isPenalty && (name == PENALTY)) || ((name == INSPECTION) && !isInspection(name, time, moves))) {
                    return@mapNotNull null
                }

                return@mapNotNull if (parts.size > 3) Step(moves, time, name, puzzle, *parts.subList(3, parts.size).toTypedArray())
                else Step(moves, time, name, puzzle)
            }
            // TODO: For time periods, make last time automatically calculate from full time
            return if ((timeInputType == TimeInputType.START_TIME) || (timeInputType == TimeInputType.START_FRAME)) {
                // TODO: Clean this up because it's a literal mess
                val mutableSteps = steps.toMutableList()
                val stepIndices = mutableSteps.indices.toMutableList()
                val iterator = stepIndices.iterator()
                var timeLeft = timeExclDNF
                while (iterator.hasNext()) {
                    val step = mutableSteps[iterator.next()]
                    if ((step.time == 0.0) || (step.time == Double.POSITIVE_INFINITY) || (step.movesSTM == 0) || (step.movesETM == 0)) {
                        iterator.remove()
                    }
                    if ((step.name == PENALTY) && (step.time != Double.POSITIVE_INFINITY)) {
                        timeLeft -= step.time
                    }
                }
                var i = -1
                while (++i < stepIndices.size) {
                    val index = stepIndices[i]
                    val step = mutableSteps[index]
                    val nextStep: Step? = if ((i + 1) >= stepIndices.size) null else mutableSteps[stepIndices[i + 1]]
                    val time = if (nextStep == null) timeLeft else nextStep.time - step.time
                    timeLeft -= time
                    mutableSteps[index] = Step(step.name, time, step.movesSTM, step.movesETM, step.moves, *step.statsToAddTo)
                }
                mutableSteps
            } else steps
        }
    }

    constructor(moves: List<Puzzle.Move>, time: Double, name: String, puzzle: Puzzle, vararg statsToAddTo: String)
            : this(name, time, puzzle.moveManager.calculateMovecountSTM(moves), puzzle.moveManager.calculateMovecountETM(moves), moves, *statsToAddTo)

    val time = round(stepTime * 100.0) / 100.0 // round time so we don't end up with incorrect statistics
    private val stps: Double = if (time <= 0) 0.0 else round((movesSTM.toDouble() / time) * 100.0) / 100.0
    private val etps: Double = if (time <= 0) 0.0 else round((movesETM.toDouble() / time) * 100.0) / 100.0

    fun isInspection(): Boolean = isInspection(name, time, moves)

    fun isPenalty(): Boolean = isPenalty(name, (movesSTM == 0) && (movesETM == 0))

    fun toReconstructionString(): String =
            if (isPenalty()) {
                "// ${getTimeAsString()} $name"
            } else {
                "${moves.joinToString(" ")} // $name".trim()
            }

    // use these for statistics so numbers are shown as 1.00 instead of 1 or 1.0000
    fun getTimeAsString(): String = timeToString(time, isPenalty())

    fun getMovesSTMAsString(): String = movesSTM.toString()

    fun getMovesETMAsString(): String = movesETM.toString()

    fun getSTPSAsString(): String = standardDecimalFormat.format(stps)

    fun getETPSAsString(): String = standardDecimalFormat.format(etps)

    class StatisticsStep(name: String, time: Double, movesSTM: Int, movesETM: Int) : Step(name, time, movesSTM, movesETM, emptyList()) {
        companion object {
            fun getMeanSteps(steps: List<List<Step>>, stepNames: List<String>): List<StatisticsStep> {
                val meanSteps = arrayListOf<StatisticsStep>()
                for (stepName in (Collections.singletonList(TOTAL) union stepNames union Collections.singletonList(PENALTY))) {
                    var count = 0
                    var time = 0.0
                    var movesSTM = 0
                    var movesETM = 0
                    for (stepList in steps) {
                        for (step in stepList) {
                            if (step.name == stepName) {
                                count++
                                time += step.time
                                movesSTM += step.movesSTM
                                movesETM += step.movesETM
                            }
                        }
                    }
                    if (count > 0) {
                        val meanTime = time / count.toDouble() // The WCA rounds for times greater than 10 minutes
                        meanSteps.add(StatisticsStep(stepName, if (meanTime >= 600.0) round(meanTime) else meanTime, round(movesSTM.toDouble() / count.toDouble()).toInt(),
                                round(movesETM.toDouble() / count.toDouble()).toInt()))
                    }
                }
                return meanSteps
            }
        }
    }
}