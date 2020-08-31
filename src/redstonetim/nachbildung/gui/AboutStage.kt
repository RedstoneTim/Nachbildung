package redstonetim.nachbildung.gui

import javafx.fxml.FXML
import javafx.scene.control.Label
import redstonetim.nachbildung.Main

/**
 * Controller for the about window.
 */
class AboutStage {
    @FXML
    private lateinit var label: Label

    @FXML
    fun initialize() {
        label.text = "${Main.TITLE} ${Main.VERSION}\nby RedstoneTim\nLicensed under GNU General Public License v3.0"
    }
}