package redstonetim.nachbildung.setting

import javafx.beans.property.*
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.gui.textfield.DoubleTextField
import redstonetim.nachbildung.gui.textfield.IntTextField
import redstonetim.nachbildung.io.JSONSerializable

interface Setting<T, S : Node> : JSONSerializable<Setting<T, S>>, Property<T> {
    val node: S
    val defaultValue: T
    val displayName: String

    fun saveChanges() {
        value = getNodeValue()
    }

    override fun toJSON(jsonWriter: JSONWriter) {
        val data = value
        if (data is JSONSerializable<*>) {
            data.toJSON(jsonWriter)
        } else {
            jsonWriter.key(name).value(data)
        }
    }

    fun setNodeValue(value: T)

    fun getNodeValue(): T

    class StringSetting(override val displayName: String, override val defaultValue: String, internalName: String) : SimpleStringProperty(null, internalName, defaultValue), Setting<String, TextField> {
        override val node = TextField()

        override fun setNodeValue(value: String) {
            node.text = value
        }

        override fun getNodeValue(): String = node.text

        override fun fromJSON(jsonObject: JSONObject): Setting<String, TextField> {
            value = jsonObject.optString(name, defaultValue)
            return this
        }
    }

    class BooleanSetting(override val displayName: String, override val defaultValue: Boolean, internalName: String)
        : SimpleBooleanProperty(null, internalName, defaultValue), Setting<Boolean, CheckBox> {
        override val node = CheckBox()

        init {
            node.isSelected = defaultValue
        }

        override fun setNodeValue(value: Boolean) {
            node.isSelected = value
        }

        override fun getNodeValue(): Boolean = node.isSelected

        override fun fromJSON(jsonObject: JSONObject): Setting<Boolean, CheckBox> {
            value = jsonObject.optBoolean(name, defaultValue)
            return this
        }
    }

    class IntSetting(override val displayName: String, override val defaultValue: Int, internalName: String)
        : SimpleIntegerProperty(null, internalName, defaultValue), Setting<Number, IntTextField> {
        override val node = IntTextField(defaultValue)

        override fun setNodeValue(value: Number) {
            node.textAsInt = value as Int
        }

        override fun getNodeValue(): Int = node.textAsInt

        override fun fromJSON(jsonObject: JSONObject): Setting<Number, IntTextField> {
            value = jsonObject.optInt(name, defaultValue)
            return this
        }
    }

    class DoubleSetting(override val displayName: String, override val defaultValue: Double, internalName: String)
        : SimpleDoubleProperty(null, internalName, defaultValue), Setting<Number, DoubleTextField> {
        override val node = DoubleTextField(defaultValue)

        override fun setNodeValue(value: Number) {
            node.textAsDouble = value as Double
        }

        override fun getNodeValue(): Double = node.textAsDouble

        override fun fromJSON(jsonObject: JSONObject): Setting<Number, DoubleTextField> {
            value = jsonObject.optDouble(name, defaultValue)
            return this
        }
    }

    class ChoiceSetting<T : JSONSerializable<T>>(override val displayName: String, override val defaultValue: T, items: Collection<T>, internalName: String)
        : SimpleObjectProperty<T>(null, internalName, defaultValue), Setting<T, ChoiceBox<T>> {
        override val node = ChoiceBox<T>()

        init {
            node.items.addAll(items)
            if (items.contains(defaultValue)) {
                node.value = defaultValue
            } else {
                if (items.isNotEmpty()) {
                    node.value = items.first()
                }
                println("The ChoiceSetting $name for the values $items does not contain the given default value $defaultValue")
            }
        }

        override fun setNodeValue(value: T) {
            node.value = value
        }

        override fun getNodeValue(): T = node.value

        override fun fromJSON(jsonObject: JSONObject): Setting<T, ChoiceBox<T>> {
            value = value.fromJSON(jsonObject)
            return this
        }
    }
}

enum class TimeInputType(private val shownName: String, val total: Boolean, val frames: Boolean) : JSONSerializable<TimeInputType> {
    TOTAL_TIMES("Total time needed", true, false), TOTAL_FRAMES("Total frames needed", true, true),
    START_TIME("Time at start", false, false), START_FRAME("Frame at start", false, true);

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("time_setting").value(name)
    }

    override fun fromJSON(jsonObject: JSONObject): TimeInputType {
        val jsonName = jsonObject.optString("time_setting")
        if (jsonName != null)
            for (setting in values())
                if (setting.name == jsonName)
                    return setting
        return TOTAL_TIMES
    }

    override fun toString(): String = shownName
}