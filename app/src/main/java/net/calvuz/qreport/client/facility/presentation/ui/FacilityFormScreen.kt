@file:OptIn(ExperimentalMaterial3Api::class)

package net.calvuz.qreport.client.facility.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QReportCard
import net.calvuz.qreport.app.app.presentation.components.QReportFormAddressSection
import net.calvuz.qreport.app.app.presentation.components.QrDropdownField
import net.calvuz.qreport.app.app.presentation.components.QrFormActionsRow
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import net.calvuz.qreport.client.facility.domain.model.FacilityType
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityFormScreen(
    modifier: Modifier = Modifier,
    clientId: String,
    facilityId: String? = null,
    onNavigateBack: () -> Unit,
    onFacilitySaved: (String) -> Unit,
    viewModel: FacilityFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(clientId, facilityId) {
        viewModel.initialize(clientId, facilityId)
    }

    LaunchedEffect(uiState.saveCompleted, uiState.savedFacilityId) {
        val savedId = uiState.savedFacilityId
        if (uiState.saveCompleted && savedId != null) {
            Timber.d("Facility saved ID: $savedId name: ${uiState.name}")
            onFacilitySaved(savedId)
            viewModel.resetSaveCompleted()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    if (facilityId == null)
                        stringResource(R.string.facility_form_title_create)
                    else
                        stringResource(R.string.facility_form_title_edit)
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.facility_form_action_back)
                    )
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && facilityId != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                FacilityFormContent(
                    uiState = uiState,
                    onFormEvent = viewModel::onFormEvent,
                    onSave = viewModel::saveFacility,
                    onCancel = onNavigateBack
                )
            }

            uiState.error?.let {
                LaunchedEffect(it) { viewModel.dismissError() }
            }
        }
    }
}

@Composable
private fun FacilityFormContent(
    uiState: FacilityFormUiState,
    onFormEvent: (FacilityFormEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dati Generali: nome, codice, tipo, note — un'unica card invece di
        // quattro campi sciolti sul fondo pagina
        QReportCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.facility_form_section_general),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                QrFormField(
                    value = uiState.name,
                    onValueChange = { onFormEvent(FacilityFormEvent.NameChanged(it)) },
                    label = stringResource(R.string.facility_form_field_name),
                    placeholder = stringResource(R.string.facility_form_field_name_placeholder),
                    errorText = uiState.nameError?.asString()
                )

                QrFormField(
                    value = uiState.code,
                    onValueChange = { onFormEvent(FacilityFormEvent.CodeChanged(it)) },
                    label = stringResource(R.string.facility_form_field_code),
                    placeholder = stringResource(R.string.facility_form_field_code_placeholder),
                    errorText = uiState.codeError?.asString()
                )

                QrDropdownField(
                    selected = uiState.facilityType,
                    options = FacilityType.entries,
                    label = stringResource(R.string.facility_form_field_type),
                    optionLabel = { stringResource(it.labelResId) },
                    onSelect = { onFormEvent(FacilityFormEvent.TypeChanged(it)) },
                    optionContent = { type ->
                        Column {
                            Text(stringResource(type.labelResId))
                            Text(
                                text = stringResource(type.descriptionResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                QrFormField(
                    value = uiState.notes,
                    onValueChange = { onFormEvent(FacilityFormEvent.NotesChanged(it)) },
                    label = stringResource(R.string.facility_form_field_notes),
                    placeholder = stringResource(R.string.facility_form_field_notes_placeholder),
                    singleLine = false,
                    maxLines = 3
                )
            }
        }

        // Address
        QReportFormAddressSection(
            street = uiState.street,
            streetNumber = uiState.streetNumber,
            city = uiState.city,
            province = uiState.province,
            postalCode = uiState.postalCode,
            country = uiState.country,
            onStreetChange = { onFormEvent(FacilityFormEvent.StreetChanged(it)) },
            onStreetNumberChange = { onFormEvent(FacilityFormEvent.StreetNumberChanged(it)) },
            onCityChange = { onFormEvent(FacilityFormEvent.CityChanged(it)) },
            onProvinceChange = { onFormEvent(FacilityFormEvent.ProvinceChanged(it)) },
            onPostalCodeChange = { onFormEvent(FacilityFormEvent.PostalCodeChanged(it)) },
            onCountryChange = { onFormEvent(FacilityFormEvent.CountryChanged(it)) }
        )

        // Options
        QReportCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.facility_form_section_options),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.facility_form_option_primary_title))
                        Text(
                            text = stringResource(R.string.facility_form_option_primary_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isPrimary,
                        onCheckedChange = { onFormEvent(FacilityFormEvent.PrimaryChanged(it)) }
                    )
                }
            }
        }

        QrFormActionsRow(
            onCancel = onCancel,
            onSave = onSave,
            saveEnabled = uiState.isFormValid,
            isSaving = uiState.isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}