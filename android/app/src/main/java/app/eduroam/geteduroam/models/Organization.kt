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
    var matchWordsLevel: Int = 0

    @IgnoredOnParcel
    var matchWords : List<String> = emptyList()

    /**
     * Improves the word matching list.
     * Each time this is called, the list will be expanded with better matching results
     * If the results cannot be improved anymore, the function will return false
     */
    fun improveMatchWords() : Boolean {
        if (matchWordsLevel == 0) {
            matchWords = listOf(nameOrId)
            matchWordsLevel = 1
            return true
        } else if (matchWordsLevel == 1) {
            // Split on anything which is non-alphanumeric
            val words = nameOrId.split(DELIMITER).filter { it.isNotEmpty() }.toMutableList()
            val abbreviation = words.map { it.first() }.joinToString("")
            words += nameOrId
            words += abbreviation
            matchWords = words
            matchWordsLevel = 2
            return true
        } else if (matchWordsLevel == 2) {
            matchWords = matchWords.map { it.removeNonSpacingMarks() }
            matchWordsLevel = 3
        }
        return false
    }

    companion object {
        private val DELIMITER = "\\W+".toRegex()
    }
}