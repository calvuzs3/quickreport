package net.calvuz.qreport.client.unit.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QrDropdownField
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import net.calvuz.qreport.client.unit.domain.model.UnitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicalUnitFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: MechanicalUnitFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    val context = LocalContext.current
    val formEvent = viewModel::onFormEvent

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar(it.asString(context)); formEvent(MechanicalUnitFormEvent.DismissError) }
    }

    val titleRes =
        if (viewModel.isEditing) R.string.unit_form_title_edit else R.string.unit_form_title_create

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.unit_form_action_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { formEvent(MechanicalUnitFormEvent.SaveForm(onSuccess = onNavigateBack)) },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (state.isValid) Icons.Default.Save else Icons.Outlined.Save,
                                contentDescription = stringResource(R.string.action_save),
                                tint = if (state.isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitTypeDropdown(selected = state.unitType, onSelected = {
                formEvent(
                    MechanicalUnitFormEvent.UnitTypeChanged(it)
                )
            })

            QrFormField(
                value = state.name,
                onValueChange = { formEvent(MechanicalUnitFormEvent.NameChanged(it)) },
                label = stringResource(R.string.unit_form_field_name),
                placeholder = stringResource(R.string.unit_form_field_name_placeholder),
                errorText = if (state.showValidation && !state.isNameValid)
                    stringResource(R.string.unit_form_error_name_required)
                else null
            )

            QrFormField(
                value = state.serialNumber,
                onValueChange = { formEvent(MechanicalUnitFormEvent.SerialNumberChanged(it)) },
                label = stringResource(R.string.unit_form_field_serial)
            )

            QrFormField(
                value = state.model,
                onValueChange = { formEvent(MechanicalUnitFormEvent.ModelChanged(it)) },
                label = stringResource(R.string.unit_form_field_model)
            )

            QrFormField(
                value = state.notes,
                onValueChange = { formEvent(MechanicalUnitFormEvent.NotesChanged(it)) },
                label = stringResource(R.string.unit_form_field_notes),
                singleLine = false,
                minLines = 2,
                maxLines = 5
            )

            Button(
                onClick = { formEvent(MechanicalUnitFormEvent.SaveForm(onSuccess = onNavigateBack)) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    if (viewModel.isEditing) stringResource(R.string.unit_form_button_save_changes)
                    else stringResource(R.string.unit_form_button_create)
                )
            }
        }
    }
}

@Composable
private fun UnitTypeDropdown(selected: UnitType, onSelected: (UnitType) -> Unit) {
    QrDropdownField(
        selected = selected,
        options = UnitType.entries,
        label = stringResource(R.string.unit_form_field_type),
        optionLabel = { stringResource(it.labelResId) },
        onSelect = onSelected
    )
}