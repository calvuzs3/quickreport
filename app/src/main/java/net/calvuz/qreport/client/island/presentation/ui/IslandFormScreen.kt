@file:OptIn(ExperimentalMaterial3Api::class)

package net.calvuz.qreport.client.island.presentation.ui

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QrDatePickerField
import net.calvuz.qreport.app.app.presentation.components.QrDropdownField
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import net.calvuz.qreport.client.island.domain.model.IslandTypeMaster
import net.calvuz.qreport.app.app.presentation.components.QrTextButton as TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandFormScreen(
    modifier: Modifier = Modifier,
    facilityId: String,
    facilityName: String,
    islandId: String? = null,
    onNavigateBack: () -> Unit,
    onIslandSaved: (String) -> Unit,
    viewModel: IslandFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(facilityId, islandId) {
        viewModel.initialize(facilityId, islandId)
    }

    LaunchedEffect(uiState.savedIslandId) {
        uiState.savedIslandId?.let { onIslandSaved(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        if (islandId == null)
                            stringResource(R.string.island_form_title_create)
                        else
                            stringResource(R.string.island_form_title_edit)
                    )
                    Text(
                        text = facilityName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.island_form_action_back)
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = { viewModel.onFormEvent(FacilityIslandFormEvent.SaveIsland) },
                    enabled = uiState.isFormValid && !uiState.isLoading
                ) {
                    Text(stringResource(R.string.island_form_action_save))
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && islandId != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                FacilityIslandFormContent(uiState = uiState, onFormEvent = viewModel::onFormEvent)
            }
            uiState.error?.let { LaunchedEffect(it) { viewModel.dismissError() } }
        }
    }
}

@Composable
private fun FacilityIslandFormContent(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IslandBasicInfoSection(uiState = uiState, onFormEvent = onFormEvent)
        TechnicalInfoSection(uiState = uiState, onFormEvent = onFormEvent)
        MaintenanceInfoSection(uiState = uiState, onFormEvent = onFormEvent)
        ConfigurationSection(uiState = uiState, onFormEvent = onFormEvent)
        NotesSection(uiState = uiState, onFormEvent = onFormEvent)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun IslandBasicInfoSection(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.island_form_section_basic), style = MaterialTheme.typography.titleMedium)

            QrFormField(
                value = uiState.serialNumber,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.SerialNumberChanged(it)) },
                label = stringResource(R.string.island_form_field_serial),
                placeholder = stringResource(R.string.island_form_field_serial_placeholder),
                errorText = uiState.serialNumberError?.asString()
            )

            IslandTypeSelector(
                availableTypes = uiState.availableIslandTypes,
                selectedTypeId = uiState.islandTypeId,
                fallbackType = uiState.islandType,
                onTypeSelected = { onFormEvent(FacilityIslandFormEvent.IslandTypeChanged(it)) }
            )

            QrFormField(
                value = uiState.modelNumber,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.ModelChanged(it)) },
                label = stringResource(R.string.island_form_field_model),
                placeholder = stringResource(R.string.island_form_field_model_placeholder),
                errorText = uiState.modelNumberError?.asString()
            )

            QrFormField(
                value = uiState.customName,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.CustomNameChanged(it)) },
                label = stringResource(R.string.island_form_field_custom_name),
                placeholder = stringResource(R.string.island_form_field_custom_name_placeholder),
                errorText = uiState.customNameError?.asString()
            )
        }
    }
}

@Composable
private fun IslandTypeSelector(
    availableTypes: List<IslandTypeMaster>,
    selectedTypeId: String?,
    fallbackType: String,
    onTypeSelected: (IslandTypeMaster) -> Unit
) {
    val selected = availableTypes.find { it.id == selectedTypeId }

    QrDropdownField(
        selected = selected,
        options = availableTypes,
        label = stringResource(R.string.island_form_field_type),
        optionLabel = { it?.label ?: fallbackType },
        onSelect = { type -> type?.let(onTypeSelected) },
        optionContent = { type ->
            Column {
                Text(type?.label ?: fallbackType, style = MaterialTheme.typography.bodyMedium)
                type?.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
private fun TechnicalInfoSection(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.island_form_section_technical), style = MaterialTheme.typography.titleMedium)

            QrDatePickerField(
                label = stringResource(R.string.island_form_field_installation_date),
                value = uiState.installationDate,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.InstallationDateChanged(it)) },
                error = uiState.installationDateError?.asString()
            )

            QrDatePickerField(
                label = stringResource(R.string.island_form_field_warranty_expiry),
                value = uiState.warrantyExpiration,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.WarrantyExpirationChanged(it)) },
                error = uiState.warrantyExpirationError?.asString()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QrFormField(
                    value = uiState.operatingHours,
                    onValueChange = { onFormEvent(FacilityIslandFormEvent.OperatingHoursChanged(it)) },
                    label = stringResource(R.string.island_form_field_operating_hours),
                    errorText = uiState.operatingHoursError?.asString(),
                    modifier = Modifier.weight(1f)
                )
                QrFormField(
                    value = uiState.cycleCount,
                    onValueChange = { onFormEvent(FacilityIslandFormEvent.CycleCountChanged(it)) },
                    label = stringResource(R.string.island_form_field_cycle_count),
                    errorText = uiState.cycleCountError?.asString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MaintenanceInfoSection(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.island_form_section_maintenance), style = MaterialTheme.typography.titleMedium)

            QrDatePickerField(
                label = stringResource(R.string.island_form_field_last_maintenance),
                value = uiState.lastMaintenanceDate,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.LastMaintenanceDateChanged(it)) },
                error = uiState.lastMaintenanceDateError?.asString()
            )
            QrDatePickerField(
                label = stringResource(R.string.island_form_field_next_maintenance),
                value = uiState.nextScheduledMaintenance,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.NextMaintenanceChanged(it)) },
                error = uiState.nextMaintenanceError?.asString()
            )
        }
    }
}

@Composable
private fun ConfigurationSection(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.island_form_section_configuration), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.island_form_option_active_title))
                    Text(
                        text = stringResource(R.string.island_form_option_active_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isActive,
                    onCheckedChange = { onFormEvent(FacilityIslandFormEvent.IsActiveChanged(it)) }
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    uiState: FacilityIslandFormUiState,
    onFormEvent: (FacilityIslandFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.island_form_section_notes), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { onFormEvent(FacilityIslandFormEvent.NotesChanged(it)) },
                label = { Text(stringResource(R.string.island_form_field_notes)) },
                placeholder = { Text(stringResource(R.string.island_form_field_notes_placeholder)) },
                isError = uiState.notesError != null,
                supportingText = if (uiState.notesError != null) {
                    { Text(uiState.notesError.asString()) }
                } else {
                    { Text(stringResource(R.string.island_form_field_notes_counter, uiState.notes.length, 1000)) }
                },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}