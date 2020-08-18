package redstonetim.nachbildung.settings

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.ReconstructionNode

// TODO: Add way to change settings and save them
object Options : JSONSerializable<Options> {
    // TODO: Load and save opened files
    // TODO: Add default puzzle and method
    val solutionSeparator = Setting.StringSetting("Solution separator", ",")
    val defaultTimeSetting = Setting.ChoiceSetting("Default time setting", TimeInputType.TOTAL_TIMES, TimeInputType.values().asList())
    private val settings = arrayOf(solutionSeparator, defaultTimeSetting)


    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.`object`()
        for(setting in settings) {
            setting.toJSON(jsonWriter)
        }
        jsonWriter.endObject()
    }

    override fun fromJSON(jsonObject: JSONObject): Options {
        for(setting in settings) {
            setting.fromJSON(jsonObject)
        }
        return this
    }
}