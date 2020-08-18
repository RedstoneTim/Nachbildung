package redstonetim.nachbildung.io

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
import redstonetim.nachbildung.Main
import redstonetim.nachbildung.ReconstructionNode
import redstonetim.nachbildung.SolveNode

interface Converter {
    companion object {
        private val converterMap = HashMap<String, Converter>()

        init {
            register(MarkdownConverter, BBCodeConverter)
        }

        fun register(vararg converters: Converter) {
            for (converter in converters) {
                converterMap[converter.toString()] = converter
            }
        }

        fun exportReconstruction(reconstruction: ReconstructionNode) = export(reconstruction)

        fun exportSolve(solve: SolveNode) = export(solve)

        private fun export(toExport: Any) {
            val exportStage = Stage()
            exportStage.initModality(Modality.APPLICATION_MODAL)
            val output = TextArea()
            output.promptText = "Output"
            output.isEditable = false

            val exportOptions = ComboBox<Converter>()
            val copyButton = Button("Copy to clipboard")
            val saveAsButton = Button("Save As")
            val closeButton = Button("Close")

            exportOptions.promptText = "Choose converter"
            exportOptions.items.addAll(converterMap.values)
            exportOptions.setOnAction {
                output.text = when(toExport) {
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
                (if (exportOptions.value == null) IOHandler.getFileChooser("Save As")
                else IOHandler.getFileChooser("Save As", *exportOptions.value.fileEndings))
                        .showSaveDialog(exportStage)?.also { it.writeText(output.text) }
            }
            closeButton.setOnAction {
                exportStage.close()
            }

            val lastRow = HBox(exportOptions, copyButton, saveAsButton, closeButton)
            lastRow.alignment = Pos.CENTER
            val content = VBox(output, lastRow)

            exportStage.title = Main.TITLE
            exportStage.scene = Scene(content)
            exportStage.showAndWait()
        }
    }
    
    val fileEndings: Array<FileChooser.ExtensionFilter>
        get() = arrayOf(FileChooser.ExtensionFilter("All files", "*.*"))

    fun processReconstruction(reconstruction: ReconstructionNode): String

    fun processSolve(solve: SolveNode): String

    override fun toString(): String
}