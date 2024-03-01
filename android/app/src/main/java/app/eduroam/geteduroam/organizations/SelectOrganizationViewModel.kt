package app.eduroam.geteduroam.organizations

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.eduroam.geteduroam.R
import app.eduroam.geteduroam.di.api.GetEduroamApi
import app.eduroam.geteduroam.extensions.removeNonSpacingMarks
import app.eduroam.geteduroam.models.Organization
import app.eduroam.geteduroam.ui.ErrorData
import app.eduroam.geteduroam.util.DatabaseHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SelectOrganizationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val api: GetEduroamApi,
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    var dbHelper: DatabaseHelper? by mutableStateOf(null)
        private set

    private var allOrganizations by mutableStateOf(emptyList<Organization>())

    val creds: MutableStateFlow<Pair<String?, String?>> =
        MutableStateFlow(Pair<String?, String?>(null, null))

    init {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch (Dispatchers.IO) {
            try {
                val response = api.getOrganizations()
                val organizationResult = response.body()
                if (response.isSuccessful && organizationResult != null) {
                    withContext(Dispatchers.Main) {
                        allOrganizations = organizationResult.instances
                        uiState = uiState.copy(isLoading = false)
                        if (uiState.filter.isNotEmpty()) {
                            onSearchTextChange(uiState.filter)
                        }
                    }
                    println("Started loading ${organizationResult.instances.size} orgs into db.")
                    dbHelper!!.loadIntoDatabase(organizationResult.instances)
                    println("Ended loading ${organizationResult.instances.size} orgs into db.")
                } else {
                    val failReason = "${response.code()}/${response.message()}]${
                        response.errorBody()?.string()
                    }"
                    Timber.w("Failed to load organizations: $failReason")
                    withContext(Dispatchers.Main) {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorData = ErrorData(
                                titleId = R.string.err_title_generic_fail,
                                messageId = R.string.err_msg_generic_unexpected_with_arg,
                                messageArg = failReason
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to get organizations")
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorData = ErrorData(
                            titleId = R.string.err_title_generic_fail,
                            messageId = R.string.err_msg_generic_unexpected_with_arg,
                            messageArg = "${e.message}/${e.javaClass.name}"
                        )
                    )
                }
            }
        }
    }

    fun initializeHelper(context: Context) {
        dbHelper = DatabaseHelper(context)
    }

    fun onStepCompleted() {}

    fun onOrganizationSelect(organization: Organization) {
        Timber.v("User has selected organization ${organization.name} (ID: ${organization.id})")
        uiState = uiState.copy(selectedOrganization = organization)
    }

    fun onSearchTextChange(filter: String) {
        println("Search start")
        val filtered = if (filter.isNotBlank()) {
            val matchingIndices = dbHelper!!.getIndicesForFilter(filter)
            matchingIndices.map {
                allOrganizations[it]
            }.sortedBy { it.nameOrId.lowercase() }
        } else {
            emptyList()
        }
        uiState = uiState.copy(filter = filter, organizations = filtered)
        println("Search end")
    }

    fun clearDialog() {

    }

    fun clearSelection() {
        uiState = uiState.copy(selectedOrganization = null, filter = "")
    }
}