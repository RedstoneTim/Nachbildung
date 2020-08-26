package redstonetim.nachbildung.step

import java.util.*
import kotlin.math.round

open class StatisticsStep(name: String, time: Double, movesSTM: Int, movesETM: Int) : Step(name, time, movesSTM, movesETM, emptyList()) {
    companion object {
        fun getMeanSteps(steps: List<List<Step>>, stepNames: List<String>): List<StatisticsStep> {
            val meanSteps = arrayListOf<StatisticsStep>()
            for (stepName in (Collections.singletonList(TOTAL) union stepNames union Collections.singletonList(SpecialStep.PenaltyStep.PENALTY))) {
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