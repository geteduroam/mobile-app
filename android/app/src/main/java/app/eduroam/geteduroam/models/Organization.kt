package app.eduroam.geteduroam.models

import android.os.Parcelable
import app.eduroam.geteduroam.extensions.removeNonSpacingMarks
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Organization(
    @Json(name = "cat_idp")
    val catIdp: Int,
    val country: String,
    val id: String,
    val name: String?,
    val profiles: List<Profile>,
) : Parcelable {
    val nameOrId get() = name ?: id
    @IgnoredOnParcel
    val matchWords: List<String>
    init {
        // Split on anything which is non-alphanumeric
        val words = nameOrId.split(DELIMITER).filter { it.isNotEmpty() }.toMutableList()
        val abbreviation = words.map { it.first() }.joinToString("")
        words += nameOrId
        words += abbreviation
        matchWords = words.map { it.removeNonSpacingMarks() }
        if (nameOrId.startsWith("eduroam Visitor")) {
            println("$nameOrId: $abbreviation")
        }
    }

    companion object {
        private val DELIMITER = "\\W+".toRegex()
    }
}