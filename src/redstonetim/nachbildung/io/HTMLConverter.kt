package redstonetim.nachbildung.io

import javafx.stage.FileChooser
import redstonetim.nachbildung.ReconstructionNode
import redstonetim.nachbildung.SolveNode

// TODO: Finish
object HTMLConverter: Converter {
    override val fileEndings: Array<FileChooser.ExtensionFilter> = arrayOf(FileChooser.ExtensionFilter("HTML files", "*.HTML"),
            FileChooser.ExtensionFilter("All files", "*.*"))

    override fun processReconstruction(reconstruction: ReconstructionNode): String {
        TODO("Not yet implemented")
    }

    override fun processSolve(solve: SolveNode): String {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "HTML"
}