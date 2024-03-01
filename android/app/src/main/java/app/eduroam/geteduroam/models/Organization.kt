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
}