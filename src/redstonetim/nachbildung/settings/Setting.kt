package redstonetim.nachbildung.settings

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.DoubleTextField
import redstonetim.nachbildung.gui.IntTextField
import redstonetim.nachbildung.io.JSONSerializable
import redstonetim.nachbildung.gui.TimeTextField
import java.util.*

abstract class Setting<T, S : Node>(private val name: String, protected val node: S, protected val initialValue: T,
                                    protected var jsonIdentifier: String = createJSONIdentifier(name)) : JSONSerializable<Setting<T, S>> {
    companion object {
        fun openSettingsStage(settings: Collection<Setting<*, *>>, onSave: () -> Unit = {}, onCancel: () -> Unit = {}) {
            val settingsStage = Stage()
            settingsStage.initModality(Modality.APPLICATION_MODAL)
            val saveButton = Button("Save")
            saveButton.setOnAction {
                onSave.invoke()
                settings.forEach { it.saveChanges() }
                settingsStage.close()
            }
            val cancelButton = Button("Cancel")
            cancelButton.setOnAction {
                onCancel.invoke()
                settingsStage.close()
            }
            val buttons = HBox(saveButton, cancelButton)
            buttons.alignment = Pos.CENTER
            val content = VBox()
            settings.forEach { it.addToSettings(content.children) }
            content.children.add(buttons)
            settingsStage.scene = Scene(content)
            settingsStage.showAndWait()
        }

        private fun createJSONIdentifier(name: String): String = name.toLowerCase(Locale.US).replace(' ', '_')
    }

    open var value: T = initialValue
    protected val group = HBox(Label(name), node)

    fun addToSettings(list: ObservableList<Node>) {
        updateNode()
        list.add(group)
    }

    open fun saveChanges() {
        value = getNodeValue()
    }

    override fun toJSON(jsonWriter: JSONWriter) {
        val data = value
        if (data is JSONSerializable<*>) {
            data.toJSON(jsonWriter)
        } else {
            jsonWriter.key(jsonIdentifier).value(data)
        }
    }

    protected abstract fun updateNode()

    protected abstract fun getNodeValue(): T

    open class StringSetting(name: String, initialValue: String, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<String, TextField>(name, TextField(), initialValue, jsonIdentifier) {
        init {
            node.promptText = name
        }

        override fun updateNode() {
            node.text = value
        }

        override fun getNodeValue(): String = node.text

        override fun fromJSON(jsonObject: JSONObject): Setting<String, TextField> {
            value = jsonObject.optString(jsonIdentifier, initialValue)
            return this
        }
    }

    open class IntSetting(name: String, initialValue: Int, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<Int, IntTextField>(name, IntTextField(), initialValue, jsonIdentifier) {
        init {
            node.promptText = name
        }

        override fun updateNode() {
            node.textAsInt = value
        }

        override fun getNodeValue(): Int = node.textAsInt

        override fun fromJSON(jsonObject: JSONObject): Setting<Int, IntTextField> {
            value = jsonObject.optInt(jsonIdentifier, initialValue)
            return this
        }
    }

    open class DoubleSetting(name: String, initialValue: Double, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<Double, DoubleTextField>(name, DoubleTextField(), initialValue, jsonIdentifier) {
        init {
            node.promptText = name
        }

        override fun updateNode() {
            node.textAsDouble = value
        }

        override fun getNodeValue(): Double = node.textAsDouble

        override fun fromJSON(jsonObject: JSONObject): Setting<Double, DoubleTextField> {
            value = jsonObject.optDouble(jsonIdentifier, initialValue)
            return this
        }
    }


    open class TimeSetting(name: String, initialValue: Double, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<Double, TimeTextField>(name, TimeTextField(), initialValue, jsonIdentifier) {
        init {
            node.promptText = name
        }

        override fun updateNode() {
            node.textAsTime = value
        }

        override fun getNodeValue(): Double = node.textAsTime

        override fun fromJSON(jsonObject: JSONObject): Setting<Double, TimeTextField> {
            value = jsonObject.optDouble(jsonIdentifier, initialValue)
            return this
        }
    }

    open class BooleanSetting(name: String, initialValue: Boolean, jsonIdentifier: String = createJSONIdentifier(name)) : Setting<Boolean, CheckBox>(name, CheckBox(), initialValue, jsonIdentifier) {
        init {
            node.isSelected = initialValue
        }

        override fun updateNode() {
            node.isSelected = value
        }

        override fun getNodeValue(): Boolean = node.isSelected

        override fun fromJSON(jsonObject: JSONObject): Setting<Boolean, CheckBox> {
            value = jsonObject.optBoolean(jsonIdentifier, initialValue)
            return this
        }
    }

    open class ChoiceSetting<T : JSONSerializable<T>>(name: String, initialValue: T, items: Collection<T>, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<T, ChoiceBox<T>>(name, ChoiceBox<T>(), initialValue, jsonIdentifier) {
        init {
            node.items.addAll(items)
            if (items.contains(initialValue)) {
                node.value = initialValue
            } else {
                if (items.isNotEmpty()) {
                    node.value = items.first()
                }
                println("The ListSetting $name for the values $items does not contain the given initial value $initialValue")
            }
        }

        override fun updateNode() {
            node.value = value
        }

        override fun getNodeValue(): T = node.value

        override fun fromJSON(jsonObject: JSONObject): Setting<T, ChoiceBox<T>> {
            value = value.fromJSON(jsonObject)
            return this
        }
    }

    open class IntChoiceSetting(name: String, initialValue: Int, range: IntRange, jsonIdentifier: String = createJSONIdentifier(name))
        : Setting<Int, ChoiceBox<Int>>(name, ChoiceBox(), initialValue, jsonIdentifier) {
        init {
            node.items.addAll(range)
            if (range.contains(initialValue)) {
                node.value = initialValue
            } else {
                if (!range.isEmpty()) {
                    node.value = range.first()
                }
                println("The ListSetting $name for the values $range does not contain the given initial value $initialValue")
            }
        }

        override fun updateNode() {
            node.value = value
        }

        override fun getNodeValue(): Int = node.value

        override fun fromJSON(jsonObject: JSONObject): Setting<Int, ChoiceBox<Int>> {
            value = jsonObject.optInt(jsonIdentifier, initialValue)
            return this
        }
    }
}

enum class TimeInputType(private val shownName: String) : JSONSerializable<TimeInputType> {
    TOTAL_TIMES("Total time needed"), TOTAL_FRAMES("Total frames needed"), START_TIME("Time at start"), START_FRAME("Frame at start");

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("time_setting").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): TimeInputType {
        val jsonName = jsonObject.optString("time_setting")
        if (jsonName != null)
            for (setting in values())
                if (setting.name == jsonName)
                    return setting
        // TODO: Fix please since it'll crash
        // TODO: Find out why this is not crashing
        return Options.defaultTimeSetting.value
    }

    override fun toString(): String = shownName
}