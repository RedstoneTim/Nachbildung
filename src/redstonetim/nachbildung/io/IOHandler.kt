package redstonetim.nachbildung.io

import javafx.stage.FileChooser
import javafx.stage.Window
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import redstonetim.nachbildung.gui.MainScene
import redstonetim.nachbildung.gui.ReconstructionNode
import redstonetim.nachbildung.method.Method
import redstonetim.nachbildung.method.MethodSubstep
import redstonetim.nachbildung.setting.Options
import java.io.File

object IOHandler {
    private val methodDirectory = File("methods")
    private val commonSubstepsDirectory = File("common_method_substeps")
    private val optionsFile = File("options.json")

    fun loadFiles() {
        fun tryToExecute(toExecute: () -> Unit) = try {
            toExecute.invoke()
        } catch (e: Exception) {
            println("An Exception occurred while reading files")
            e.printStackTrace()
        }
        tryToExecute { loadCommonSubsteps() }
        tryToExecute { loadMethods() }
        tryToExecute { loadOptions() }
    }

    fun saveFiles() {
        try {
            saveOptions()
        } catch (e: Exception) {
            println("An Exception occurred while writing files")
            e.printStackTrace()
        }
    }

    private fun parseMethodSubstep(json: JSONObject?): MethodSubstep? {
        return if (json == null) null
        else {
            val stepName = json.optString("name")
            val regex = json.optString("regex")?.let { Regex(it) }
            if (stepName == null) null
            else MethodSubstep(stepName, json.optString("identifier").ifBlank { stepName }) { steps, currentIndex ->
                ((regex != null) && steps[currentIndex].name.matches(regex))
            }
        }
    }

    private fun loadCommonSubsteps() {
        if (commonSubstepsDirectory.isDirectory) {
            for (file in commonSubstepsDirectory.listFiles() ?: return) {
                if (file != null) {
                    try {
                        parseMethodSubstep(JSONObject(JSONTokener(file.inputStream())))?.register()
                    } catch (e: JSONException) {
                        println("A JSONException occurred while reading a common substep file:")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun loadMethods() {
        if (methodDirectory.isDirectory) {
            for (file in methodDirectory.listFiles() ?: emptyArray()) {
                if (file != null) {
                    try {
                        val json = JSONObject(JSONTokener(file.inputStream()))
                        val methodName = json.optString("name")
                        val jsonSteps = json.optJSONArray("steps")
                        if ((methodName?.isEmpty() == false) && (jsonSteps != null)) {
                            val methodSubsteps = arrayListOf<MethodSubstep>()
                            for (i in 0 until jsonSteps.length()) {

                                val substep = parseMethodSubstep(jsonSteps.optJSONObject(i))
                                if (substep == null) {
                                    val substepIdentifier = jsonSteps.optString(i)
                                    if (substepIdentifier != null) {
                                        MethodSubstep[substepIdentifier]?.let { methodSubsteps.add(it)  }
                                    }
                                } else {
                                    methodSubsteps.add(substep)
                                }
                            }
                            Method(methodName, methodSubsteps).register()
                        }
                    } catch (e: JSONException) {
                        println("A JSONException occurred while reading a method file:")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun loadOptions() {
        if (optionsFile.isFile) {
            try {
                Options.fromJSON(JSONObject(JSONTokener(optionsFile.inputStream())))
            } catch (e: JSONException) {
                println("A JSONException occurred while reading the options file:")
                e.printStackTrace()
            }
        } else {
            saveOptions()
        }
    }

    fun saveOptions() {
        val json = JSONStringer()
        Options.toJSON(json)
        optionsFile.writeText(json.toString())
    }

    fun openReconstructions(files: Collection<File>?) {
        if (files != null) {
            var firstFile = false
            for (file in files) {
                try {
                    val json = JSONObject(JSONTokener(file.inputStream()))
                    val reconstruction = ReconstructionNode.create()
                    reconstruction.fileLocation = file
                    reconstruction.fromJSON(json)
                    reconstruction.savedProperty.value = true
                    MainScene.instance.addReconstruction(reconstruction)
                    if (!firstFile) {
                        firstFile = true
                        MainScene.instance.setSelectedReconstruction(reconstruction)
                    }
                } catch (e: JSONException) {
                    println("A JSONException occurred while reading a reconstruction file:")
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveReconstructionAs(reconstruction: ReconstructionNode, file: File?) {
        try {
            if (file != null) {
                reconstruction.fileLocation = file
                val jsonWriter = JSONStringer()
                reconstruction.toJSON(jsonWriter)
                file.writeText(jsonWriter.toString())
                reconstruction.savedProperty.value = true
            }
        } catch (e: JSONException) {
            println("A JSONException occurred while saving a reconstruction file:")
            e.printStackTrace()
        }
    }

    private fun createFileChooser(title: String, vararg extensionFilters: FileChooser.ExtensionFilter =
            arrayOf(FileChooser.ExtensionFilter("Text files", "*.txt"),
                    FileChooser.ExtensionFilter("All files", "*.*"))): FileChooser {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.initialDirectory = File(Options.defaultSaveDirectory.value)
        fileChooser.extensionFilters.addAll(*extensionFilters)
        return fileChooser
    }

    fun showOpenFilesDialog(title: String, ownerWindow: Window, vararg extensionFilters: FileChooser.ExtensionFilter =
            arrayOf(FileChooser.ExtensionFilter("Text files", "*.txt"),
                    FileChooser.ExtensionFilter("All files", "*.*"))): List<File>? {
        return createFileChooser(title, *extensionFilters).showOpenMultipleDialog(ownerWindow)
    }

    fun showSaveFileDialog(title: String, ownerWindow: Window, initialFileName: String, vararg extensionFilters: FileChooser.ExtensionFilter =
            arrayOf(FileChooser.ExtensionFilter("Text files", "*.txt"),
                    FileChooser.ExtensionFilter("All files", "*.*"))): File? {
        val fileChooser = createFileChooser(title, *extensionFilters)
        fileChooser.initialFileName = initialFileName
        val file = fileChooser.showSaveDialog(ownerWindow)
        if (file?.parent != null) {
            Options.defaultSaveDirectory.value = file.parent
        }
        return file
    }
}