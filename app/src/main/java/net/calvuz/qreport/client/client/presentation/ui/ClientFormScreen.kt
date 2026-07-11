package net.calvuz.qreport.client.client.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.error.presentation.UiText
import net.calvuz.qreport.app.app.presentation.components.QReportFormAddressSection
import net.calvuz.qreport.app.app.presentation.components.QReportCard
import net.calvuz.qreport.app.app.presentation.components.QrFormActionsRow
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onClientSaved: (String, String) -> Unit,
    clientId: String? = null,   // null = create, non-null = edit
    viewModel: ClientFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle save completed
    LaunchedEffect(uiState.saveCompleted, uiState.savedClientId) {
        if (uiState.saveCompleted && !uiState.savedClientId.isNullOrBlank()) {
            Timber.d("Client saved ID: ${uiState.savedClientId}")
            Timber.d("Client saved NAME: ${uiState.savedClientName}")
            onClientSaved(uiState.savedClientId!!, uiState.savedClientName!!)
            viewModel.resetSaveCompleted()
        }
    }

    // Initialize for edit mode
    LaunchedEffect(clientId) {
        if (clientId != null) viewModel.initForEdit(clientId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (uiState.isEditMode)
                            stringResource(R.string.client_form_title_edit)
                        else
                            stringResource(R.string.client_form_title_create),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = uiState.companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.client_form_action_back)
                    )
                }
            }
        )

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading while loading client for edit
            if (uiState.isLoading && clientId != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {

                // Section 1: Company Data (nome + note — un solo campo a testa
                // non giustifica due card separate, ognuna col proprio titolo)
                item {
                    CompanyDataSection(
                        companyName = uiState.companyName,
                        companyNameError = uiState.companyNameError,
                        notes = uiState.notes,
                        onEvent = viewModel::onFormEvent
                    )
                }

                // Section 2: Address
                item {
                    QReportFormAddressSection(
                        street = uiState.street,
                        streetNumber = uiState.streetNumber,
                        city = uiState.city,
                        province = uiState.province,
                        postalCode = uiState.postalCode,
                        country = uiState.country,
                        onStreetChange = { viewModel.onFormEvent(ClientFormEvent.StreetChanged(it)) },
                        onStreetNumberChange = { viewModel.onFormEvent(ClientFormEvent.StreetNumberChanged(it)) },
                        onCityChange = { viewModel.onFormEvent(ClientFormEvent.CityChanged(it)) },
                        onProvinceChange = { viewModel.onFormEvent(ClientFormEvent.ProvinceChanged(it)) },
                        onPostalCodeChange = { viewModel.onFormEvent(ClientFormEvent.PostalCodeChanged(it)) },
                        onCountryChange = { viewModel.onFormEvent(ClientFormEvent.CountryChanged(it)) }
                    )
                }

                // Action buttons
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    QrFormActionsRow(
                        onCancel = onNavigateBack,
                        onSave = viewModel::saveClient,
                        saveEnabled = uiState.canSave,
                        isSaving = uiState.isSaving,
                        cancelText = stringResource(R.string.client_form_button_cancel),
                        saveText = if (clientId == null)
                            stringResource(R.string.client_form_button_create)
                        else
                            stringResource(R.string.client_form_button_save_changes)
                    )
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: show snackbar using error.asString(context)
        }
    }
}

// =============================================================================
// PRIVATE COMPOSABLES
// =============================================================================

@Composable
private fun CompanyDataSection(
    modifier: Modifier = Modifier,
    companyName: String,
    companyNameError: UiText? = null,
    notes: String,
    onEvent: (ClientFormEvent) -> Unit
) {
    QReportCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.client_form_section_company),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            QrFormField(
                value = companyName,
                onValueChange = { onEvent(ClientFormEvent.CompanyNameChanged(it)) },
                label = stringResource(R.string.client_form_field_company_name),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters
                ),
                errorText = companyNameError?.asString()
            )

            QrFormField(
                value = notes,
                onValueChange = { onEvent(ClientFormEvent.NotesChanged(it)) },
                label = stringResource(R.string.client_form_field_notes),
                placeholder = stringResource(R.string.client_form_field_notes_placeholder),
                singleLine = false,
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        }
    }
}