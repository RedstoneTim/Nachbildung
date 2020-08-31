package redstonetim.nachbildung.io

import org.json.JSONObject
import org.json.JSONWriter

/**
 * Interface that allows for saving and loading objects from JSON.
 */
interface JSONSerializable<T> {
    /**
     * Saves this object to JSON by writing the data to the [JSONWriter].
     */
    fun toJSON(jsonWriter: JSONWriter)

    /**
     * Returns a (not necessarily new) object of type [T] using the data from the given [JSONObject].
     */
    fun fromJSON(jsonObject: JSONObject): T
}