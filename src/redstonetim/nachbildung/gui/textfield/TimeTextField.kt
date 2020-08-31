package redstonetim.nachbildung.gui.textfield

import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import redstonetim.nachbildung.step.Step

/**
 * A [TextField] that only accepts time values.
 */
class TimeTextField(value: Double = 0.0, val acceptDNF: Boolean = false) : TextField(Step.timeToString(value)) {

    private val timeFormatter = TextFormatter<String> {
        if (it.controlNewText.isNotEmpty()) {
            if (!Step.isValidTime(it.controlNewText, acceptDNF)) {
                return@TextFormatter null
            }
        }
        it
    }

    init {
        textFormatter = timeFormatter
    }

    /**
     * The text in a time ([Double]) format.
     */
    var textAsTime: Double
        get() = Step.parseTime(text)
        set(value) {
            text = Step.timeToString(value)
        }
}