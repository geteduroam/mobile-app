package app.eduroam.geteduroam.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class InstitutionResult(
    val instances: List<Institution>,
    val version: Int,
) : Parcelable
