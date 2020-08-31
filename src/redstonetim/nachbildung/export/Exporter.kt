package redstonetim.nachbildung.export

import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONWriter
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.gui.ReconstructionNode
import redstonetim.nachbildung.gui.SolveNode
import redstonetim.nachbildung.io.IOHandler
import redstonetim.nachbildung.io.JSONSerializable

/**
 * Interface for converting reconstructions and solves to a specific format.
 */
interface Exporter : JSONSerializable<Exporter> {
    companion object {
        val converterMap = HashMap<String, Exporter>()

        init {
            for (converter in arrayOf(MarkdownExporter, BBCodeExporter, TxtExporter)) {
                converterMap[converter.toString()] = converter
            }
        }

        /**
         * Opens a window to export the given reconstruction.
         */
        fun exportReconstruction(reconstruction: ReconstructionNode) {
            reconstruction.update(true)
            export(reconstruction, reconstruction.getSuggestedFileName())
        }

        /**
         * Opens a window to export the given solve.
         */
        fun exportSolve(solve: SolveNode) {
            solve.update(true)
            export(solve, solve.getSuggestedFileName())
        }

        private fun export(toExport: Any, suggestedFileName: String) {
            val exportStage = Stage()
            exportStage.initModality(Modality.APPLICATION_MODAL)
            val output = TextArea()
            output.style = "-fx-font-family: 'monospaced';"
            output.promptText = "Output"
            output.isEditable = false

            val exportOptions = ComboBox<Exporter>()
            val copyButton = Button("Copy to clipboard")
            val saveAsButton = Button("Save As")
            val closeButton = Button("Close")

            exportOptions.promptText = "Choose converter"
            exportOptions.items.addAll(converterMap.values)
            exportOptions.setOnAction {
                output.text = when (toExport) {
                    is ReconstructionNode -> exportOptions.value.processReconstruction(toExport)
                    is SolveNode -> exportOptions.value.processSolve(toExport)
                    else -> "An issue occurred: The value $toExport is neither a reconstruction nor a solve.".also { println(it) }
                }
                copyButton.isDisable = false
                saveAsButton.isDisable = false
            }

            copyButton.isDisable = true
            copyButton.setOnAction {
                val content = ClipboardContent()
                content.putString(output.text)
                Clipboard.getSystemClipboard().setContent(content)
            }
            saveAsButton.isDisable = true
            saveAsButton.setOnAction {
                val extension = exportOptions.value.fileEndings.getOrNull(0)?.extensions?.getOrNull(0)
                IOHandler.showSaveFileDialog("Save As", exportStage,
                        "$suggestedFileName.${extension?.substring(extension.lastIndexOf('.') + 1) ?: "txt"}",
                        *exportOptions.value.fileEndings)?.writeText(output.text)
            }
            closeButton.setOnAction {
                exportStage.close()
            }

            val lastRow = HBox(exportOptions, copyButton, saveAsButton, closeButton)
            lastRow.alignment = Pos.CENTER
            val content = VBox(output, lastRow)
            exportStage.title = Main.TITLE
            exportStage.scene = Scene(content)
            exportOptions.requestFocus()
            exportStage.showAndWait()
        }
    }

    /**
     * The file endings that should show up when saving the output.
     */
    val fileEndings: Array<FileChooser.ExtensionFilter>
        get() = arrayOf(FileChooser.ExtensionFilter("Text files", ".txt"), FileChooser.ExtensionFilter("All files", "*.*"))

    /**
     * Returns the converted reconstruction.
     */
    fun processReconstruction(reconstruction: ReconstructionNode): String

    /**
     * Returns the converted solve.
     */
    fun processSolve(solve: SolveNode): String

    override fun toString(): String

    override fun toJSON(jsonWriter: JSONWriter) {
        jsonWriter.key("converter").value(toString())
    }

    override fun fromJSON(jsonObject: JSONObject): Exporter = converterMap[jsonObject.optString("converter")]
            ?: MarkdownExporter
}