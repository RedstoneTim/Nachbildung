package redstonetim.nachbildung.gui.textfield

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition
import java.util.*

/**
 * A [TextField] that only accepts [Double] values
 */
class DoubleTextField() : TextField() {
    constructor(value: Double): this() {
        textAsDouble = value
    }

    private val timeFormat = DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.US))
    private val timeFormatter = TextFormatter<String> { c ->
        if (c.controlNewText.isNotEmpty()) {
            val parsePosition = ParsePosition(0)
            val number = timeFormat.parse(c.controlNewText, parsePosition)
            if (number == null || parsePosition.index < c.controlNewText.length) {
                return@TextFormatter null
            }
        }
        c
    }

    init {
        textFormatter = timeFormatter
    }

    var textAsDouble: Double
        get() = text.toDoubleOrNull() ?: 0.0
        set(value) {
            text = value.toString()
        }
}