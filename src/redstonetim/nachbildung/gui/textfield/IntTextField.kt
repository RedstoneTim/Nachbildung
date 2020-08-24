package redstonetim.nachbildung.gui.textfield

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition
import java.util.*

/**
 * A [TextField] that only accepts [Int] values
 */
class IntTextField() : TextField() {
    constructor(value: Int): this() {
        textAsInt = value
    }

    private val timeFormat = DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.US))
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

    var textAsInt: Int
        get() = text.toIntOrNull() ?: 0
        set(value) {
            text = value.toString()
        }
}