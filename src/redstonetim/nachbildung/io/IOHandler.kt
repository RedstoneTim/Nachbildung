package redstonetim.nachbildung.io

import javafx.stage.FileChooser
import jdk.nashorn.internal.objects.NativeRegExp.compile
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import org.json.JSONTokener
import redstonetim.nachbildung.MainScene
import redstonetim.nachbildung.ReconstructionNode
import redstonetim.nachbildung.Step
import redstonetim.nachbildung.puzzle.Method
import redstonetim.nachbildung.settings.Options
import java.io.File
import java.io.FileReader
import java.util.function.Predicate
import javax.script.ScriptEngineManager

object IOHandler {
    private val methodDirectory = File("methods")
    private val optionsFile = File("options.json")

    fun loadFiles() {
        try {
            loadMethods()
            loadOptions()
        } catch (e: Exception) {
            println("An Exception occurred while reading files")
            e.printStackTrace()
        }
    }

    // TODO: Move to [Method]?
    private fun loadMethods() {
        if (methodDirectory.isDirectory) {
            for (file in methodDirectory.listFiles() ?: emptyArray()) {
                if (file != null) {
                    try {
                        val json = JSONObject(JSONTokener(file.inputStream()))
                        val methodName = json.optString("name")
                        val jsonSteps = json.optJSONArray("steps")
                        if ((methodName?.isEmpty() == false) && (jsonSteps != null)) {
                            val methodSteps = LinkedHashMap<String, Predicate<Step>>()
                            for (i in 0 until jsonSteps.length()) {
                                val jsonObject = jsonSteps.optJSONObject(i)
                                val stepName = jsonObject?.optString("name")
                                val regex = jsonObject?.optString("regex")?.let { Regex(it) }
                                val kts = jsonObject?.optString("kts")
                                if (kts != null) {
                                    //ScriptEngineManager().getEngineByExtension("kts").eval(FileReader(kts))
                                }
                                // TODO: Implement JS or any other kind of script
                                // or maybe not idk
                                if (stepName != null) {
                                    methodSteps[stepName] = Predicate {
                                        ((regex != null) && it.name.matches(regex))
                                    }
                                }
                            }
                            Method(methodName, methodSteps).register()
                        }
                    } catch (e: JSONException) {
                        println("A JSONException occurred while reading a method file:")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // TODO: Move to [Method]?
    private fun loadOptions() {
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

    private fun saveOptions() {
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
                    val reconstruction = ReconstructionNode.create(file.nameWithoutExtension)
                    reconstruction.fileLocation = file
                    reconstruction.fromJSON(json)
                    reconstruction.saved = true
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
                reconstruction.saved = true
            }
        } catch (e: JSONException) {
            println("A JSONException occurred while saving a reconstruction file:")
            e.printStackTrace()
        }
    }

    fun getFileChooser(title: String, vararg extensionFilters: FileChooser.ExtensionFilter =
            arrayOf(FileChooser.ExtensionFilter("Text files", "*.txt"),
                    FileChooser.ExtensionFilter("All files", "*.*"))): FileChooser {
        val fileChooser = FileChooser()
        fileChooser.title = title
        fileChooser.initialDirectory = File(System.getProperty("user.home"))
        fileChooser.extensionFilters.addAll(*extensionFilters)
        return fileChooser
    }
}