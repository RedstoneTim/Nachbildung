package redstonetim.nachbildung.setting

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.MainScene
import redstonetim.nachbildung.io.IOHandler
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.method.Method
import redstonetim.nachbildung.puzzle.Puzzle
import java.io.File

/**
 * A list of default options.
 */
object Options : JSONSerializable<Options> {
    val defaultSolutonSeparator = Setting.StringSetting("Default solution separator", ",", "solution_separator")
    val defaultPuzzle = Setting.ChoiceSetting("Default puzzle", Puzzle.puzzle3x3x3, Puzzle.values, "puzzle")
    val defaultMethod = Setting.ChoiceSetting("Default method", Method["CFOP"]!!, Method.values, "method")
    val defaultFPS = Setting.DoubleSetting("Default FPS", 30.0, "fps")
    val defaultTimeSetting = Setting.ChoiceSetting("Default time setting", TimeInputType.TOTAL_TIMES, TimeInputType.values().asList(), "default_time_setting")
    val defaultAutoFillStepsSetting = Setting.BooleanSetting("Default auto fill steps (W.I.P.)", false, "auto_fill_steps")
    private val visibleSettings = listOf<Setting<*, *>>(defaultSolutonSeparator, defaultPuzzle, defaultMethod, defaultFPS, defaultTimeSetting, defaultAutoFillStepsSetting)

    // TODO: Fix this not working
    val defaultSaveDirectory = Setting.StringSetting("Default save directory", System.getProperty("user.home"), "save_directory")
    val openedReconstructions = object : JSONSerializable<List<File>> {
        override fun toJSON(jsonWriter: JSONWriter) {
            jsonWriter.key("openedReconstructions").array()
            MainScene.instance.getReconstructions().mapNotNull { it.fileLocation }.forEach {
                if (it.exists()) {
                    jsonWriter.value(it.path)
                }
            }
            jsonWriter.endArray()
        }

        override fun fromJSON(jsonObject: JSONObject): List<File> {
            return jsonObject.optJSONArray("openedReconstructions")?.mapNotNull {
                if (it is String) {
                    val file = File(it)
                    if (file.exists()) {
                        return@mapNotNull file
                    }
                }
                null
            }?.also { IOHandler.reconstructionsToOpen.addAll(it) } ?: emptyList()
        }
    }
    private val invisibleSettings = listOf<JSONSerializable<*>>(defaultSaveDirectory, openedReconstructions)

    fun openSettingsDialog() {
        SettingsStage.open(visibleSettings, onSave = { IOHandler.saveOptions() })
    }

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.`object`()
        for (setting in visibleSettings) {
            setting.toJSON(jsonWriter)
        }
        for (setting in invisibleSettings) {
            setting.toJSON(jsonWriter)
        }
        jsonWriter.endObject()
    }

    override fun fromJSON(jsonObject: JSONObject): Options {
        for (setting in visibleSettings) {
            setting.fromJSON(jsonObject)
        }
        for (setting in invisibleSettings) {
            setting.fromJSON(jsonObject)
        }
        return this
    }
}