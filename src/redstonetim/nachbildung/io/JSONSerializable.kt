package redstonetim.nachbildung.io

import org.json.JSONObject
import org.json.JSONWriter

interface JSONSerializable<T> {
    fun toJSON(jsonWriter: JSONWriter)

    fun fromJSON(jsonObject: JSONObject): T
}