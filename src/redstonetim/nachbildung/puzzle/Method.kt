package redstonetim.nachbildung.puzzle

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.Step
import java.util.function.Predicate

// TODO: Maybe associate with puzzle?
class Method(private val name: String, private val steps: LinkedHashMap<String, Predicate<Step>>) : JSONSerializable<Method> {
    companion object : HashMap<String, Method>() {
        // TODO: Use options for default method
        fun getDefaultMethod(): Method = Method["CFOP"]!!
    }

    init {
        Method[name] = this
    }

    fun register() {
        if (name.isEmpty()) {
            println("It was attempted to register a method with an empty name.")
        } else {
            put(name, this)
        }
    }

    override fun toString(): String = name

    /**
     * Returns all step names unique to this method ("Total" and "Penalty" are not included)
     */
    fun getStepNames(): List<String> = steps.map { it.key }.toList()

    fun getStatisticsSteps(solveSteps: List<Step>): List<Step.StatisticsStep> {
        val statisticsSteps = ArrayList<Step.StatisticsStep>()

        var penaltyTime = 0.0
        var totalTime = 0.0
        var totalMovesSTM = 0
        var totalMovesETM = 0
        for (solveStep in solveSteps) {
            if (!solveStep.isInspection()) {
                totalTime += solveStep.time
                totalMovesSTM += solveStep.movesSTM
                totalMovesETM += solveStep.movesETM
                if (solveStep.isPenalty()) {
                    penaltyTime += solveStep.time
                }
            }
        }
        statisticsSteps.add(Step.StatisticsStep(Step.TOTAL, totalTime, totalMovesSTM, totalMovesETM))

        for (step in steps) {
            var time = 0.0
            var movesSTM = 0
            var movesETM = 0
            for (solveStep in solveSteps) {
                if (!solveStep.isInspection() && (!solveStep.isPenalty()) && (step.value.test(solveStep) || solveStep.statsToAddTo.contains(step.key))) {
                    time += solveStep.time
                    movesSTM += solveStep.movesSTM
                    movesETM += solveStep.movesETM
                }
            }
            if (time > 0 || movesSTM > 0 || movesETM > 0) {
                statisticsSteps.add(Step.StatisticsStep(step.key, time, movesSTM, movesETM))
            }
        }

        if (penaltyTime > 0) {
            statisticsSteps.add(Step.StatisticsStep(Step.PENALTY, penaltyTime, 0, 0))
        }
        return statisticsSteps
    }

    fun getStepName(scramble: String, moves: String): String = TODO("Add system to automatically get step names")

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("method").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): Method = Method[jsonObject.optString("method")]
            ?: getDefaultMethod()
}