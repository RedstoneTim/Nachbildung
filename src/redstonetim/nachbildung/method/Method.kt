package redstonetim.nachbildung.method

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.Puzzle

/**
 * Represents a method that is used for solving a [Puzzle].
 */
open class Method(val name: String, val autoFillSteps: List<String>, val steps: List<MethodSubstep>) : JSONSerializable<Method> {
    companion object : HashMap<String, Method>() {
        init {
            // basic CFOP implementation
            Method("CFOP", emptyList(), arrayListOf(
                    MethodSubstep("F2L") { steps, index ->
                        val stepName = steps[index].name
                        stepName.contains("Cross") || stepName.contains("pair")
                    },
                    MethodSubstep("LL") { steps, index -> steps[index].name.contains("LL") },
                    MethodSubstep("OLS") { steps, index ->
                        val stepName = steps[index].name
                        stepName.contains("OLL") || stepName.contains("4th pair")
                    },
                    MethodSubstep("PLL") { steps, index -> steps[index].name.contains("PLL") }
            )).register()
        }
    }

    fun register() {
        put(name, this)
    }

    override fun toString(): String = name

    /**
     * Returns all step names unique to this method ("Total" and "Penalty" are not included)
     */
    fun getStepNames(): List<String> = steps.map { it.name }.toList()

    open fun getStepName(scrambleMoves: List<Puzzle.Move>, moves: List<Puzzle.Move>, index: Int): String? = autoFillSteps.getOrNull(index)

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("method").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): Method = Method[jsonObject.optString("method")]
            ?: Method["CFOP"]!!
}