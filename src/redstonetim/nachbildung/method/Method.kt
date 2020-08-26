package redstonetim.nachbildung.method

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.puzzle.Puzzle

open class Method(val name: String, val steps: List<MethodSubstep>) : JSONSerializable<Method> {
    companion object : HashMap<String, Method>()

    fun register() {
        put(name, this)
    }

    override fun toString(): String = name

    /**
     * Returns all step names unique to this method ("Total" and "Penalty" are not included)
     */
    fun getStepNames(): List<String> = steps.map { it.name }.toList()

    open fun getStepName(scrambleMoves: List<Puzzle.Move>, moves: String): String = TODO("Add system to automatically get step names")

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("method").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): Method = Method[jsonObject.optString("method")]
            ?: Method["CFOP"]!!
}