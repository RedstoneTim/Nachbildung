package redstonetim.nachbildung.setting

import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.io.IOHandler
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.method.Method
import redstonetim.nachbildung.puzzle.Puzzle

object Options : JSONSerializable<Options> {
    val solutionSeparator = Setting.StringSetting("Solution separator", ",", "solution_separator")
    val defaultPuzzle = Setting.ChoiceSetting("Default puzzle", Puzzle.puzzle3x3x3, Puzzle.values, "puzzle")
    val defaultMethod = Setting.ChoiceSetting("Default method", Method["CFOP"]!!, Method.values, "method")
    val defaultFPS = Setting.DoubleSetting("Default FPS", 30.0, "fps")
    val defaultTimeSetting = Setting.ChoiceSetting("Default time setting", TimeInputType.TOTAL_TIMES, TimeInputType.values().asList(), "default_time_setting")
    val defaultAutoFillStepsSetting = Setting.BooleanSetting("", false, "auto_fill_steps")
    private val visibleSettings = listOf<Setting<*, *>>(solutionSeparator, defaultPuzzle, defaultMethod, defaultFPS, defaultTimeSetting, defaultAutoFillStepsSetting)

    val defaultSaveDirectory = Setting.StringSetting("Default save directory", System.getProperty("user.home"), "save_directory")
    private val invisibleSettings = listOf<Setting<*, *>>(defaultSaveDirectory)

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