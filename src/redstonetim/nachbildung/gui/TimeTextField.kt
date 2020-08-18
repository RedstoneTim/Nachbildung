package redstonetim.nachbildung.gui

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import redstonetim.nachbildung.Step

/**
 * A [TextField] that only accepts time values
 */
class TimeTextField : TextField() {
    private val timeFormatter = TextFormatter<String> { c ->
        if (c.controlNewText.isNotEmpty()) {
            if (!Step.isValidTime(c.controlNewText)) {
                return@TextFormatter null
            }
        }
        c
    }

    init {
        textFormatter = timeFormatter
    }

    var textAsTime: Double
        get() = Step.parseTime(text)
        set(value) {
            text = Step.timeToString(value)
        }
}