package net.calvuz.qreport.checkup.checkup.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.checkup.checkup.domain.model.CheckUpHeader
import net.calvuz.qreport.checkup.checkup.domain.model.ClientInfo
import net.calvuz.qreport.client.island.domain.model.IslandInfo
import net.calvuz.qreport.settings.domain.model.TechnicianInfo
import net.calvuz.qreport.app.app.presentation.components.QrFormActionsRow
import net.calvuz.qreport.app.app.presentation.components.QrFormField
import net.calvuz.qreport.app.app.presentation.components.SectionCard
import net.calvuz.qreport.settings.presentation.ui.TechnicianSettingsViewModel
import net.calvuz.qreport.app.app.presentation.components.QrOutlinedButton as OutlinedButton

/**
 * Dialog per l'editing delle informazioni dell'header del check-up
 */
@Suppress("ParamsComparedByRef")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHeaderDialog(
    header: CheckUpHeader,
    onDismiss: () -> Unit,
    onConfirm: (CheckUpHeader) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    technicianViewModel: TechnicianSettingsViewModel = hiltViewModel()
) {
    // Client Info State
    var companyName by remember { mutableStateOf(header.clientInfo.companyName) }
    var contactPerson by remember { mutableStateOf(header.clientInfo.contactPerson) }
    var site by remember { mutableStateOf(header.clientInfo.site) }
    var address by remember { mutableStateOf(header.clientInfo.address) }
    var phone by remember { mutableStateOf(header.clientInfo.phone) }
    var email by remember { mutableStateOf(header.clientInfo.email) }

    // Island Info State
    var serialNumber by remember { mutableStateOf(header.islandInfo.serialNumber) }
    var model by remember { mutableStateOf(header.islandInfo.model) }
    var installationDate by remember { mutableStateOf(header.islandInfo.installationDate) }
    var lastMaintenanceDate by remember { mutableStateOf(header.islandInfo.lastMaintenanceDate) }
    var operatingHours by remember { mutableStateOf(header.islandInfo.operatingHours.toString()) }
    var cycleCount by remember { mutableStateOf(header.islandInfo.cycleCount.toString()) }

    // Technician Info State
    var technicianName by remember { mutableStateOf(header.technicianInfo.name) }
    var technicianCompany by remember { mutableStateOf(header.technicianInfo.company) }
    var certification by remember { mutableStateOf(header.technicianInfo.certification) }
    var technicianPhone by remember { mutableStateOf(header.technicianInfo.phone) }
    var technicianEmail by remember { mutableStateOf(header.technicianInfo.email) }

    // ===== TECHNICIAN SETTINGS INTEGRATION =====
    val technicianSettings by technicianViewModel.currentTechnicianInfo.collectAsStateWithLifecycle()
    val hasTechnicianData by technicianViewModel.hasTechnicianData.collectAsStateWithLifecycle()

    // Notes
    var notes by remember { mutableStateOf(header.notes) }

    // ===== AUTO-LoadError TECHNICIAN DATA =====
    var isAutoLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(technicianSettings) {
        // Solo se i campi tecnico sono vuoti E ci sono settings salvate
        if (technicianName.isBlank() &&
            technicianCompany.isBlank() &&
            hasTechnicianData &&
            (technicianSettings.name.isNotBlank() || technicianSettings.company.isNotBlank())
        ) {
            technicianName = technicianSettings.name
            technicianCompany = technicianSettings.company
            certification = technicianSettings.certification
            technicianPhone = technicianSettings.phone
            technicianEmail = technicianSettings.email
            isAutoLoaded = true
        }
    }

    // Validation
    val isValidForm = companyName.isNotBlank() && serialNumber.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.checkup_dialog_edit_header_title
                        ), // "Modifica Informazioni",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.checkup_dialog_edit_header_close) // "Chiudi"
                        )
                    }
                }

                HorizontalDivider()

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // ===== AUTO-LoadError SUCCESS MESSAGE =====
                    if (isAutoLoaded) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.7f
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.checkup_dialog_edit_header_autoload_message), //"Dati tecnico caricati automaticamente dal profilo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Client Information Section
                    SectionCard(
                        title = stringResource(R.string.checkup_dialog_edit_header_section_client), // "Informazioni Cliente",
                        icon = Icons.Default.Business
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QrFormField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_client_company_label),
                                errorText = if (companyName.isBlank()) "" else null
                            )

                            QrFormField(
                                value = contactPerson,
                                onValueChange = { contactPerson = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_client_contact_label)
                            )

                            QrFormField(
                                value = site,
                                onValueChange = { site = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_client_site_label)
                            )

                            QrFormField(
                                value = address,
                                onValueChange = { address = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_client_address_label),
                                singleLine = false,
                                maxLines = 2
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QrFormField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_client_phone_label),
                                    modifier = Modifier.weight(1f)
                                )

                                QrFormField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_client_email_label),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Island Information Section
                    SectionCard(
                        title = stringResource(R.string.checkup_dialog_edit_header_section_island), //"Informazioni Isola",
                        icon = Icons.Default.PrecisionManufacturing
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QrFormField(
                                value = serialNumber,
                                onValueChange = { serialNumber = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_island_serial_label),
                                errorText = if (serialNumber.isBlank()) "" else null
                            )

                            QrFormField(
                                value = model,
                                onValueChange = { model = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_island_model_label)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QrFormField(
                                    value = installationDate,
                                    onValueChange = { installationDate = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_island_installation_label),
                                    modifier = Modifier.weight(1f),
                                    placeholder = stringResource(R.string.checkup_dialog_edit_header_island_installation_placeholder)
                                )

                                QrFormField(
                                    value = lastMaintenanceDate,
                                    onValueChange = { lastMaintenanceDate = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_island_maintenance_label),
                                    modifier = Modifier.weight(1f),
                                    placeholder = stringResource(R.string.checkup_dialog_edit_header_island_maintenance_placeholder)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QrFormField(
                                    value = operatingHours,
                                    onValueChange = { value ->
                                        if (value.all { it.isDigit() } || value.isEmpty()) {
                                            operatingHours = value
                                        }
                                    },
                                    label = stringResource(R.string.checkup_dialog_edit_header_island_hours_label),
                                    modifier = Modifier.weight(1f)
                                )

                                QrFormField(
                                    value = cycleCount,
                                    onValueChange = { value ->
                                        if (value.all { it.isDigit() } || value.isEmpty()) {
                                            cycleCount = value
                                        }
                                    },
                                    label = stringResource(R.string.checkup_dialog_edit_header_island_cycles_label),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Technician Information Section
                    SectionCard(
                        title = stringResource(R.string.checkup_dialog_edit_header_section_technician),
                        icon = Icons.Default.Engineering // .EngineeringOutlined
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // Load from Profile Button (if data available and not auto-loaded)
                            if (hasTechnicianData && !isAutoLoaded) {
                                OutlinedButton(
                                    onClick = {
                                        technicianName = technicianSettings.name
                                        technicianCompany = technicianSettings.company
                                        certification = technicianSettings.certification
                                        technicianPhone = technicianSettings.phone
                                        technicianEmail = technicianSettings.email
                                        isAutoLoaded = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.checkup_dialog_edit_header_technician_load_profile))
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            QrFormField(
                                value = technicianName,
                                onValueChange = { technicianName = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_technician_name_label)
                            )

                            QrFormField(
                                value = technicianCompany,
                                onValueChange = { technicianCompany = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_technician_company_label)
                            )

                            QrFormField(
                                value = certification,
                                onValueChange = { certification = it },
                                label = stringResource(R.string.checkup_dialog_edit_header_technician_certification_label)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QrFormField(
                                    value = technicianPhone,
                                    onValueChange = { technicianPhone = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_technician_phone_label),
                                    modifier = Modifier.weight(1f)
                                )

                                QrFormField(
                                    value = technicianEmail,
                                    onValueChange = { technicianEmail = it },
                                    label = stringResource(R.string.checkup_dialog_edit_header_technician_email_label),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Notes Section
                    SectionCard(
                        title = stringResource(R.string.checkup_dialog_edit_header_section_notes),
                        icon = Icons.AutoMirrored.Default.Notes
                    ) {
                        QrFormField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = stringResource(R.string.checkup_dialog_edit_header_notes_label),
                            singleLine = false,
                            maxLines = 3,
                            placeholder = stringResource(R.string.checkup_dialog_edit_header_notes_placeholder)
                        )
                    }
                }

                // Actions
                HorizontalDivider()

                QrFormActionsRow(
                    onCancel = onDismiss,
                    onSave = {
                        val updatedHeader = header.copy(
                            clientInfo = ClientInfo(
                                companyName = companyName,
                                contactPerson = contactPerson,
                                site = site,
                                address = address,
                                phone = phone,
                                email = email
                            ),
                            islandInfo = IslandInfo(
                                serialNumber = serialNumber,
                                model = model,
                                installationDate = installationDate,
                                lastMaintenanceDate = lastMaintenanceDate,
                                operatingHours = operatingHours.toIntOrNull() ?: 0,
                                cycleCount = cycleCount.toLongOrNull() ?: 0L
                            ),
                            technicianInfo = TechnicianInfo(
                                name = technicianName,
                                company = technicianCompany,
                                certification = certification,
                                phone = technicianPhone,
                                email = technicianEmail
                            ),
                            notes = notes
                        )
                        onConfirm(updatedHeader)
                    },
                    modifier = Modifier.padding(24.dp),
                    saveEnabled = isValidForm,
                    isSaving = isLoading,
                    cancelText = stringResource(R.string.checkup_dialog_edit_header_action_cancel),
                    saveText = stringResource(R.string.checkup_dialog_edit_header_action_save)
                )
            }
        }
    }
}