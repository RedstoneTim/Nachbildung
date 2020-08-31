package redstonetim.nachbildung.step

import redstonetim.nachbildung.method.Method
import redstonetim.nachbildung.puzzle.Puzzle
import redstonetim.nachbildung.setting.TimeInputType
import redstonetim.nachbildung.step.Step.Companion.TOTAL

/**
 * Utility class for parsing [Step]s.
 */
object StepParser {
    /**
     * Parses a list of [Step]s from the given input.
     */
    fun parseStepsFromText(solution: String, timeExclDNF: Double, method: Method, autoFillStep: Boolean, scrambleMoves: List<Puzzle.Move>, puzzle: Puzzle,
                           fps: Double, timeInputType: TimeInputType, solutionSeparator: String): List<Step>? {
        val trimmedSolution = solution.trim()
        if (trimmedSolution.isEmpty()) {
            // Don't give any errors if the solution is empty
            return emptyList()
        } else {
            val performedMoves = arrayListOf<Puzzle.Move>()
            val offset = arrayListOf<Puzzle.Move>()
            var autoFillIndex = 0
            val steps = trimmedSolution.split('\n').mapNotNull { line ->
                val parts = line.split(solutionSeparator).map { it.trim() }
                // Parse moves. If they aren't constituted of valid notation, return null (i.e. indicate that an error occurred during parsing).
                val moves = puzzle.moveManager.parseMoves(parts.getOrElse(1) { "" })?.let {
                    if (offset.isEmpty()) it else puzzle.moveManager.offsetMoves(it, offset)
                } ?: return@parseStepsFromText null
                // add moves for auto-fill
                performedMoves.addAll(moves)

                // Parse name. If the name is not present or cannot be inferred, return null.
                val rawName = parts.getOrElse(2) { "" }
                val name = if (rawName.isBlank()) {
                    if (autoFillStep) {
                        method.getStepName(scrambleMoves, performedMoves, autoFillIndex++)
                                ?: return@parseStepsFromText null
                    } else return@parseStepsFromText null
                } else {
                    rawName
                }

                // Parse raw time. If no time is given, interpret it as 0. If the time cannot be parsed, return null.
                val textTime = parts.getOrNull(0)?.ifBlank { "0" } ?: "0"
                val rawTime = if (Step.isValidTime(textTime)) {
                    Step.parseTime(textTime)
                } else return@parseStepsFromText null
                val time = if (timeInputType.frames) rawTime / fps else rawTime

                // Test for special steps. If one roughly matches and a new instance can be created, return that.
                // If one roughly matches but no instance can be created, indicate error by returning null.
                for (specialStep in SpecialStep.specialSteps) {
                    if (specialStep.roughlyMatches(name, rawTime, time, moves)) {
                        val step = specialStep.tryCreateInstance(name, rawTime, time, moves)
                        if (step == null) {
                            return@parseStepsFromText null
                        } else {
                            return@mapNotNull if (step is SpecialStep.OffsetStep) {
                                // For offset, add moves and don't include offset step anywhere.
                                offset.addAll(moves)
                                null
                            } else step
                        }
                    }
                }

                // Normal steps shouldn't be allowed to have DNF times
                if (!Step.isValidTime(textTime, false)) {
                    return@parseStepsFromText null
                }

                return@mapNotNull if (parts.size > 3) Step(moves, time, name, puzzle, *parts.subList(3, parts.size).toTypedArray())
                else Step(moves, time, name, puzzle)
            }.toMutableList()
            return if (timeInputType.total) {
                val index = steps.indexOfLast { (it !is SpecialStep) && (it.time == 0.0) }
                if (index >= 0) {
                    val step = steps[index]
                    steps[index] = Step(step.name,
                            timeExclDNF - (steps.filter { (it !is SpecialStep) || ((it is SpecialStep.PenaltyStep) && (it.time != Double.POSITIVE_INFINITY)) }
                                    .map { it.time }.sum()), step.movesSTM, step.movesETM, step.moves, *step.statsToAddTo)
                }
                steps
            } else {
                // TODO: Clean this up at some point
                val stepIndices = steps.indices.toMutableList()
                val iterator = stepIndices.iterator()
                var timeLeft = timeExclDNF
                while (iterator.hasNext()) {
                    val step = steps[iterator.next()]
                    if (step is SpecialStep) {
                        iterator.remove()
                    }
                    if ((step is SpecialStep.PenaltyStep) && (step.time != Double.POSITIVE_INFINITY)) {
                        timeLeft -= step.time
                    }
                }
                var i = -1
                while (++i < stepIndices.size) {
                    val index = stepIndices[i]
                    val step = steps[index]
                    val nextStep: Step? = if ((i + 1) >= stepIndices.size) null else steps[stepIndices[i + 1]]
                    val time = if (nextStep == null) timeLeft else nextStep.time - step.time
                    timeLeft -= time
                    steps[index] = Step(step.name, time, step.movesSTM, step.movesETM, step.moves, *step.statsToAddTo)
                }
                steps
            }
        }
    }

    /**
     * Gets the [StatisticsStep]s from a list of normal [Step]s using the given [Method].
     */
    fun getStatisticsSteps(solveSteps: List<Step>, method: Method): List<StatisticsStep> {
        val statisticsSteps = ArrayList<StatisticsStep>()

        var penaltyTime = 0.0
        var totalTime = 0.0
        var totalMovesSTM = 0
        var totalMovesETM = 0
        for (solveStep in solveSteps) {
            if (solveStep !is SpecialStep) {
                totalTime += solveStep.time
                totalMovesSTM += solveStep.movesSTM
                totalMovesETM += solveStep.movesETM
            } else if (solveStep is SpecialStep.PenaltyStep) {
                penaltyTime += solveStep.time
            }
        }

        statisticsSteps.add(StatisticsStep(TOTAL, totalTime + penaltyTime, totalMovesSTM, totalMovesETM, totalTime))
        for (substep in method.steps) {
            var time = 0.0
            var movesSTM = 0
            var movesETM = 0
            for (i in solveSteps.indices) {
                val solveStep = solveSteps[i]
                if ((solveStep !is SpecialStep) && (substep.test(solveSteps, i) || solveStep.statsToAddTo.contains(substep.name))) {
                    time += solveStep.time
                    movesSTM += solveStep.movesSTM
                    movesETM += solveStep.movesETM
                }
            }
            if (time > 0 || movesSTM > 0 || movesETM > 0) {
                statisticsSteps.add(StatisticsStep(substep.name, time, movesSTM, movesETM, time))
            }
        }

        if (penaltyTime > 0) {
            statisticsSteps.add(object : StatisticsStep(SpecialStep.PenaltyStep.PENALTY, penaltyTime, 0, 0, 0.0) {
                override fun getTimeAsString(): String = SpecialStep.PenaltyStep.timePenaltyAsString(time)
            })
        }
        return statisticsSteps
    }
}