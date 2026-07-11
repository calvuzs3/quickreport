@file:OptIn(ExperimentalMaterial3Api::class)

package net.calvuz.qreport.client.island.maintenance.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QrDateTimePickerField
import net.calvuz.qreport.app.app.presentation.components.QrDropdownField
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import net.calvuz.qreport.client.island.maintenance.domain.model.MaintenanceOperationType
import net.calvuz.qreport.client.island.maintenance.domain.model.MaintenanceOutcome
import net.calvuz.qreport.client.unit.domain.model.MechanicalUnit
import net.calvuz.qreport.app.app.presentation.components.QrTextButton as TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogFormScreen(
    modifier: Modifier = Modifier,
    islandId: String,
    onNavigateBack: () -> Unit,
    onLogSaved: () -> Unit,
    viewModel: MaintenanceLogFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onFormEvent: (MaintenanceLogFormEvent) -> Unit = viewModel::onFormEvent

    LaunchedEffect(islandId) {
        viewModel.initialize(islandId)
    }

    // Navigate back after successful save
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onLogSaved()
    }

    // Dismiss error automatically
    LaunchedEffect(uiState.error) {
        uiState.error?.let { viewModel.dismissError() }
    }

    // Intercept system back button
    BackHandler {
        onFormEvent(MaintenanceLogFormEvent.BackPressed)
    }

    // ── Unsaved changes dialog ────────────────────────────────────────────────
    if (uiState.showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { onFormEvent(MaintenanceLogFormEvent.DismissUnsavedDialog) },
            title = { Text(stringResource(R.string.maint_form_unsaved_title)) },
            text = { Text(stringResource(R.string.maint_form_unsaved_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onFormEvent(MaintenanceLogFormEvent.ConfirmDiscard)
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.maint_form_unsaved_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { onFormEvent(MaintenanceLogFormEvent.DismissUnsavedDialog) }) {
                    Text(stringResource(R.string.maint_form_unsaved_keep_editing))
                }
            }
        )
    }

    // ── Save confirmation dialog ──────────────────────────────────────────────
    if (uiState.showSaveConfirmation) {
        AlertDialog(
            onDismissRequest = { onFormEvent(MaintenanceLogFormEvent.DismissSaveConfirmation) },
            title = { Text(stringResource(R.string.maint_form_save_confirmation_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.maint_form_save_confirmation_message))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onFormEvent(MaintenanceLogFormEvent.UpdateMaintenanceOnSaveChanged(!uiState.updateMaintenanceOnSave))
                        }
                    ) {
                        Checkbox(
                            checked = uiState.updateMaintenanceOnSave,
                            onCheckedChange = { onFormEvent(MaintenanceLogFormEvent.UpdateMaintenanceOnSaveChanged(it)) }
                        )
                        Text(stringResource(R.string.maint_form_save_update_maintenance_label))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onFormEvent(MaintenanceLogFormEvent.ConfirmSaveLog) }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onFormEvent(MaintenanceLogFormEvent.DismissSaveConfirmation) }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
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
                    Text(stringResource(R.string.maint_screen_log_form_title_create))
                    Text(
                        text = uiState.islandName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onFormEvent(MaintenanceLogFormEvent.BackPressed) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.island_form_action_back)
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = { onFormEvent(MaintenanceLogFormEvent.SaveLog) },
                    enabled = uiState.isFormValid && !uiState.isLoading
                ) {
                    Text(stringResource(R.string.island_form_action_save))
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                MaintenanceLogFormContent(uiState = uiState, onFormEvent = onFormEvent)
            }
        }
    }
}

// =============================================================================
// FORM CONTENT
// =============================================================================

@Composable
private fun MaintenanceLogFormContent(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OperationSection(uiState = uiState, onFormEvent = onFormEvent)
        ComponentSection(uiState = uiState, onFormEvent = onFormEvent)
        DescriptionSection(uiState = uiState, onFormEvent = onFormEvent)
        OutcomeSection(uiState = uiState, onFormEvent = onFormEvent)
        MachineStateSection(uiState = uiState, onFormEvent = onFormEvent)
        TechnicianSection(uiState = uiState, onFormEvent = onFormEvent)
        NotesSection(uiState = uiState, onFormEvent = onFormEvent)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// =============================================================================
// SECTIONS
// =============================================================================

@Composable
private fun OperationSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maint_label_operation_type),
                style = MaterialTheme.typography.titleMedium
            )

            // Date + time picker
            QrDateTimePickerField(
                label = stringResource(R.string.maint_label_performed_at),
                value = uiState.performedAt,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.PerformedAtChanged(it)) },
                error = uiState.performedAtError?.asString()
            )

            // Operation type dropdown
            OperationTypeDropdown(
                selected = uiState.operationType,
                onSelected = { onFormEvent(MaintenanceLogFormEvent.OperationTypeChanged(it)) }
            )

            // Custom label — visible only when OTHER is selected
            if (uiState.isCustomLabelRequired) {
                QrFormField(
                    value = uiState.customOperationLabel,
                    onValueChange = { onFormEvent(MaintenanceLogFormEvent.CustomOperationLabelChanged(it)) },
                    label = stringResource(R.string.maint_label_custom_operation),
                    errorText = uiState.customOperationLabelError?.asString()
                )
            }
        }
    }
}

@Composable
private fun OperationTypeDropdown(
    selected: MaintenanceOperationType,
    onSelected: (MaintenanceOperationType) -> Unit
) {
    QrDropdownField(
        selected = selected,
        options = MaintenanceOperationType.entries,
        label = stringResource(R.string.maint_label_operation_type),
        optionLabel = { stringResource(it.labelResId) },
        onSelect = onSelected
    )
}

@Composable
private fun ComponentSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maint_label_component),
                style = MaterialTheme.typography.titleMedium
            )

            // Unit selector — shown only when units are available
            if (uiState.availableUnits.isNotEmpty()) {
                UnitDropdown(
                    units = uiState.availableUnits,
                    selectedUnitId = uiState.selectedUnitId,
                    onUnitSelected = { onFormEvent(MaintenanceLogFormEvent.UnitSelected(it)) },
                    onUnitCleared = { onFormEvent(MaintenanceLogFormEvent.UnitCleared) }
                )
            }

            // Free text — active when no unit is selected from FK list
            if (uiState.isFreeTextComponentActive) {
                QrFormField(
                    value = uiState.componentLabel,
                    onValueChange = { onFormEvent(MaintenanceLogFormEvent.ComponentLabelChanged(it)) },
                    label = if (uiState.availableUnits.isNotEmpty())
                        stringResource(R.string.maint_label_component_not_listed)
                    else
                        stringResource(R.string.maint_label_component_free)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    units: List<MechanicalUnit>,
    selectedUnitId: String?,
    onUnitSelected: (String) -> Unit,
    onUnitCleared: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedUnit = units.firstOrNull { it.id == selectedUnitId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUnit?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.maint_label_component_unit)) },
            placeholder = { Text(stringResource(R.string.maint_label_component_not_listed)) },
            trailingIcon = {
                if (selectedUnit != null) {
                    IconButton(onClick = onUnitCleared) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(unit.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = stringResource(unit.unitType.labelResId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = { onUnitSelected(unit.id); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.maint_label_description),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            QrFormField(
                value = uiState.description,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.DescriptionChanged(it)) },
                label = stringResource(R.string.maint_label_description),
                errorText = uiState.descriptionError?.asString(),
                singleLine = false,
                maxLines = 5
            )
        }
    }
}

@Composable
private fun OutcomeSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maint_label_outcome),
                style = MaterialTheme.typography.titleMedium
            )

            // Segmented button for outcome
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MaintenanceOutcome.entries.forEachIndexed { index, outcome ->
                    SegmentedButton(
                        selected = uiState.outcome == outcome,
                        onClick = { onFormEvent(MaintenanceLogFormEvent.OutcomeChanged(outcome)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = MaintenanceOutcome.entries.size
                        ),
                        label = {
                            Text(
                                text = stringResource(outcome.labelResId),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // Duration field
            QrFormField(
                value = uiState.durationMinutes,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.DurationChanged(it)) },
                label = stringResource(R.string.maint_label_duration),
                errorText = uiState.durationError?.asString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun MachineStateSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.island_form_section_technical),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QrFormField(
                    value = uiState.operatingHoursAtEvent,
                    onValueChange = { onFormEvent(MaintenanceLogFormEvent.OperatingHoursChanged(it)) },
                    label = stringResource(R.string.maint_label_operating_hours),
                    errorText = uiState.operatingHoursError?.asString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                QrFormField(
                    value = uiState.cycleCountAtEvent,
                    onValueChange = { onFormEvent(MaintenanceLogFormEvent.CycleCountChanged(it)) },
                    label = stringResource(R.string.maint_label_cycle_count),
                    errorText = uiState.cycleCountError?.asString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TechnicianSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maint_label_technician),
                style = MaterialTheme.typography.titleMedium
            )
            QrFormField(
                value = uiState.technicianName,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.TechnicianNameChanged(it)) },
                label = stringResource(R.string.maint_label_technician),
                errorText = uiState.technicianNameError?.asString()
            )
            QrFormField(
                value = uiState.technicianCompany,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.TechnicianCompanyChanged(it)) },
                label = stringResource(R.string.maint_label_technician_company)
            )
        }
    }
}

@Composable
private fun NotesSection(
    uiState: MaintenanceLogFormUiState,
    onFormEvent: (MaintenanceLogFormEvent) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.island_form_section_notes),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            QrFormField(
                value = uiState.notes,
                onValueChange = { onFormEvent(MaintenanceLogFormEvent.NotesChanged(it)) },
                label = stringResource(R.string.maint_label_notes),
                singleLine = false,
                maxLines = 4
            )
        }
    }
}