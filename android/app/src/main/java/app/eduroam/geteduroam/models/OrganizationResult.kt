package app.eduroam.geteduroam.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class OrganizationResult(
    val instances: List<Organization>,
    val version: Int,
) : Parcelable
