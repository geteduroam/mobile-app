package app.eduroam.geteduroam.config.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.serialization.Serializable
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Text


@Serializable
class ProviderLogo {

    @field:Text
    var value: String? = null

    @field:Attribute
    var mime: String? = null

    @field:Attribute
    var encoding: String? = null

    fun convertToBitmap() : Bitmap? {
        if (encoding?.lowercase() == "base64") {
            val binary = Base64.decode(value, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(binary, 0, binary.size)
        }
        return null
    }
}
