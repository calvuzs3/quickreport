@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("HardCodedStringLiteral")

package net.calvuz.qreport.app.app.presentation.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.calvuz.qreport.app.app.presentation.ui.theme.Spacing
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QReportCard
import net.calvuz.qreport.app.app.presentation.components.QrListStatItem
import net.calvuz.qreport.app.app.presentation.components.QrLoadingState
import net.calvuz.qreport.app.app.presentation.components.StatItemOrientation
import net.calvuz.qreport.app.app.presentation.ui.theme.success
import net.calvuz.qreport.checkup.checkup.domain.model.CheckUp
import net.calvuz.qreport.checkup.checkup.presentation.model.CheckupPkg
import net.calvuz.qreport.checkup.status.domain.model.CheckUpStatusMaster
import net.calvuz.qreport.client.client.domain.model.Client
import net.calvuz.qreport.client.client.presentation.model.ClientPkg
import net.calvuz.qreport.client.island.domain.model.Island
import net.calvuz.qreport.client.island.domain.model.IslandTypeMaster
import net.calvuz.qreport.client.island.presentation.model.IslandPkg
import net.calvuz.qreport.client.island.presentation.model.resolveIslandTypeDisplay

@Suppress("ParamsComparedByRef")@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToClients: () -> Unit,
    onNavigateToCheckUps: () -> Unit,
    onNavigateToIslands: () -> Unit,
    onNavigateToIslandDetail: (facilityId: String, islandId: String) -> Unit,
    onNavigateToTechnicalInterventions: () -> Unit,
    @Suppress("unused") onNavigateToNewCheckUp: () -> Unit,
    onNavigateToCheckUpDetail: (String) -> Unit,
    onNavigateToHomePreferences: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.selectedCheckUpId) {
        uiState.selectedCheckUpId?.let { checkUpId ->
            onNavigateToCheckUpDetail(checkUpId)
            viewModel.clearSelectedCheckUp()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        HomeHeader(
            onRefresh = viewModel::refresh,
            isLoading = uiState.isLoading,
            onNavigateToPreferences = onNavigateToHomePreferences,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = 12.dp)
        )

        HorizontalDivider()

        if (uiState.isLoading) {
            QrLoadingState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                // ── ACCESSO RAPIDO ────────────────────────────────────────────
                item {
                    Text(
                        text = stringResource(R.string.home_section_management),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        NavTile(modifier = Modifier.weight(1f), title = stringResource(ClientPkg.titleResId), icon = ClientPkg.icon, onClick = onNavigateToClients)
                        //NavTile(modifier = Modifier.weight(1f), title = stringResource(CheckupPkg.titleResId), icon = CheckupPkg.icon, onClick = onNavigateToCheckUps)
                        NavTile(modifier = Modifier.weight(1f), title = (CheckupPkg.title), icon = CheckupPkg.icon, onClick = onNavigateToCheckUps)
                        NavTile(modifier = Modifier.weight(1f), title = stringResource(R.string.home_nav_interventions_title), icon = Icons.Default.Workspaces, onClick = onNavigateToTechnicalInterventions)
                    }
                }

                item { Spacer(modifier = Modifier.height(Spacing.md)) }

                // ── CHECK-UP ─────────────────────────────────────────────────
                item {
                    Text(
                        text = stringResource(R.string.home_section_checkup),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )

                    val stats = uiState.checkupStats
                    if (stats != null) {
                        val hasCritical = stats.criticalCheckUps > 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                QrListStatItem(
                                    value = stats.totalCheckUps.toString(),
                                    label = stringResource(R.string.home_checkup_stat_total),
                                    orientation = StatItemOrientation.Vertical,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                )
                            }
                            QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                QrListStatItem(
                                    value = stats.activeCheckUps.toString(),
                                    label = stringResource(R.string.home_checkup_stat_active),
                                    orientation = StatItemOrientation.Vertical,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                )
                            }
                            QReportCard(
                                modifier = Modifier.weight(1f),
                                tickColor = if (hasCritical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ) {
                                QrListStatItem(
                                    value = stats.criticalCheckUps.toString(),
                                    label = stringResource(R.string.home_checkup_stat_critical),
                                    orientation = StatItemOrientation.Vertical,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = stringResource(R.string.home_section_checkup_recent),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )

                    if (uiState.recentCheckUps.isEmpty()) {
                        PreviewEmptyRow(stringResource(R.string.home_checkup_empty))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            uiState.recentCheckUps.take(3).forEach { checkUp ->
                                CheckUpPreviewRow(
                                    checkUp = checkUp,
                                    statusMasters = uiState.statusMasters,
                                    criticalIssues = uiState.recentCheckUpsCriticalCounts[checkUp.id] ?: 0,
                                    onClick = { viewModel.navigateToCheckUp(checkUp.id) }
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(Spacing.md)) }

                // ── CLIENTI (parametrizzata da Preferenze Home) ───────────────
                val prefs = uiState.homePreferences
                if (prefs.showClientStats || prefs.showRecentClients) {
                    item {
                        Text(
                            text = stringResource(R.string.home_section_clients),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = Spacing.sm)
                        )

                        val clientStats = uiState.clientStats
                        if (prefs.showClientStats && clientStats != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                    QrListStatItem(
                                        value = clientStats.totalClient.toString(),
                                        label = stringResource(R.string.home_clients_stat_total),
                                        orientation = StatItemOrientation.Vertical,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                    )
                                }
                                QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                    QrListStatItem(
                                        value = clientStats.activeClient.toString(),
                                        label = stringResource(R.string.home_clients_stat_active),
                                        orientation = StatItemOrientation.Vertical,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                    )
                                }
                            }
                        }

                        if (prefs.showRecentClients) {
                            if (prefs.showClientStats) {
                                Spacer(modifier = Modifier.height(Spacing.md))
                            }
                            Text(
                                text = stringResource(R.string.home_section_clients_recent),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )
                            if (uiState.recentClients.isEmpty()) {
                                PreviewEmptyRow(stringResource(R.string.home_clients_empty))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    uiState.recentClients.forEach { client ->
                                        ClientPreviewRow(
                                            client = client,
                                            onClick = onNavigateToClients
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(Spacing.md)) }
                
                // ── ISOLE (parametrizzata da Preferenze Home) ─────────────────
                if (prefs.showIslandStats || prefs.showRecentIslands) {
                    item {
                        Text(
                            text = stringResource(R.string.home_section_islands),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = Spacing.sm)
                        )

                        if (prefs.showIslandStats) {
                            val islandStats = uiState.islandStats
                            val hasMaintenance = islandStats.maintenanceSoon > 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                    QrListStatItem(
                                        value = islandStats.total.toString(),
                                        label = stringResource(R.string.home_islands_stat_total),
                                        orientation = StatItemOrientation.Vertical,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                    )
                                }
                                QReportCard(modifier = Modifier.weight(1f), tickColor = MaterialTheme.colorScheme.outline) {
                                    QrListStatItem(
                                        value = islandStats.operational.toString(),
                                        label = stringResource(R.string.home_islands_stat_operational),
                                        orientation = StatItemOrientation.Vertical,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                    )
                                }
                                QReportCard(
                                    modifier = Modifier.weight(1f),
                                    tickColor = if (hasMaintenance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ) {
                                    QrListStatItem(
                                        value = islandStats.maintenanceSoon.toString(),
                                        label = stringResource(R.string.home_islands_stat_maintenance),
                                        orientation = StatItemOrientation.Vertical,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
                                    )
                                }
                            }
                        }

                        if (prefs.showRecentIslands) {
                            if (prefs.showIslandStats) {
                                Spacer(modifier = Modifier.height(Spacing.md))
                            }
                            Text(
                                text = stringResource(R.string.home_section_islands_recent),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )

                            if (uiState.recentIslands.isEmpty()) {
                                PreviewEmptyRow(stringResource(R.string.home_islands_empty))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    uiState.recentIslands.forEach { island ->
                                        IslandPreviewRow(
                                            island = island,
                                            islandTypes = uiState.islandTypes,
                                            onClick = { onNavigateToIslandDetail(island.facilityId, island.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(Spacing.md)) }
            }
        }
    }
}

// =============================================================================
// HEADER
// =============================================================================

@Composable
private fun HomeHeader(
    @Suppress("unused") onRefresh: () -> Unit,
    @Suppress("unused") isLoading: Boolean,
    onNavigateToPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography
                .headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onNavigateToPreferences) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = stringResource(R.string.home_action_preferences),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// PREVIEW ROWS
// =============================================================================

@Composable
private fun PreviewEmptyRow(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Suppress("ParamsComparedByRef")@Composable
private fun CheckUpPreviewRow(checkUp: CheckUp, statusMasters: List<CheckUpStatusMaster>, criticalIssues: Int, onClick: () -> Unit) {
    // Striscia = severità (design-system.md: "striscia neutra = normale, striscia
    // arancio piena = attenzione"), NON il colore configurato per lo stato — un
    // check-up "In corso" senza criticità resta neutro come uno "Completato".
    val statusMaster = statusMasters.find { it.id == checkUp.status }
    val hasCritical = criticalIssues > 0
    val stripeColor = if (hasCritical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val subtitle = buildString {
        append(checkUp.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())
        append(" · ")
        append(statusMaster?.label ?: "?")
        if (hasCritical) {
            append(" — $criticalIssues ")
            append(if (criticalIssues == 1) "critica" else "critiche")
        }
    }
    QReportCard(onClick = onClick) {
        // IntrinsicSize.Min: senza, la Row riceve un vincolo di altezza
        // illimitato dalla Column del Card, e fillMaxHeight() sul Box della
        // striscia collassa a zero invece di adattarsi al contenuto (stesso
        // pattern corretto già in uso in CheckUpDetailScreen.kt).
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Text(
                    text = checkUp.header.clientInfo.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Suppress("ParamsComparedByRef")@Composable
private fun ClientPreviewRow(client: Client, onClick: () -> Unit) {
    val statusColor = if (client.isActive) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.outline
    QReportCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Icon(imageVector = ClientPkg.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Column {
                        Text(text = client.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        client.headquarters?.city?.let { city ->
                            Text(text = city, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Icon(
                    imageVector = if (client.isActive) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Suppress("ParamsComparedByRef")@Composable
private fun IslandPreviewRow(island: Island, islandTypes: List<IslandTypeMaster>, onClick: () -> Unit) {
    val typeDisplay = resolveIslandTypeDisplay(island.islandTypeId, islandTypes)
    val statusColor = if (island.needsMaintenance()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.success
    QReportCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Icon(imageVector = typeDisplay.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Column {
                        Text(text = island.customName ?: island.serialNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = typeDisplay.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    imageVector = if (island.needsMaintenance()) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =============================================================================
// NAV TILES (bottom row)
// =============================================================================

@Composable
private fun NavTile(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Stessa "quick-action" del mockup (design/mockup-orange-technical.html,
    // .quick-action/.ic-tile): card piatta bordo 1dp, icona su tile arancio
    // pieno con inchiostro grafite — nessun "highlight" speciale, le 3 tile
    // sono equivalenti.
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(9.dp), // stesso raggio di QReportCard
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
//                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}