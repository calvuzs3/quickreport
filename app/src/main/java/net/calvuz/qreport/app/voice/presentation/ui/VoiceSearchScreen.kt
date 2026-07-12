package net.calvuz.qreport.app.voice.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.EmptyState
import net.calvuz.qreport.app.app.presentation.components.QReportErrorState
import net.calvuz.qreport.app.app.presentation.components.QrLoadingState

/**
 * Landing screen for App Actions voice deep links (e.g. "apri il contatto di [cliente]",
 * "indirizzo dello stabilimento [nome]"). Resolves the free-text spoken query against
 * clients/facilities via [VoiceSearchViewModel] and either auto-navigates on a single
 * unambiguous match or lets the user pick from a disambiguation list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSearchScreen(
    modifier: Modifier = Modifier,
    mode: VoiceSearchMode,
    query: String,
    onNavigateToContactList: (clientId: String, clientName: String) -> Unit,
    onNavigateToFacilityDetail: (clientId: String, facilityId: String) -> Unit,
    onNavigateToIslandDetail: (facilityId: String, islandId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VoiceSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mode, query) {
        viewModel.search(mode, query)
    }

    // Auto-navigate straight through when the spoken query resolves to exactly one match.
    LaunchedEffect(uiState.clientResults, uiState.facilityResults, uiState.islandResults) {
        when (mode) {
            VoiceSearchMode.CONTACT -> uiState.clientResults.singleOrNull()?.let { client ->
                onNavigateToContactList(client.id, client.companyName)
            }

            VoiceSearchMode.FACILITY -> uiState.facilityResults.singleOrNull()?.let { facility ->
                onNavigateToFacilityDetail(facility.clientId, facility.id)
            }

            VoiceSearchMode.ISLAND -> uiState.islandResults.singleOrNull()?.let { island ->
                onNavigateToIslandDetail(island.facilityId, island.id)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.voice_search_title)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(R.string.voice_search_action_back)
                    )
                }
            }
        )

        val hasResults = when (mode) {
            VoiceSearchMode.CONTACT -> uiState.clientResults.isNotEmpty()
            VoiceSearchMode.FACILITY -> uiState.facilityResults.isNotEmpty()
            VoiceSearchMode.ISLAND -> uiState.islandResults.isNotEmpty()
        }

        when {
            uiState.isLoading -> QrLoadingState(
                message = stringResource(R.string.voice_search_loading, query)
            )

            uiState.error != null -> QReportErrorState(
                error = uiState.error!!,
                onRetry = { viewModel.search(mode, query) },
                onDismiss = onNavigateBack
            )

            !hasResults -> EmptyState(
                textTitle = stringResource(R.string.voice_search_empty_title),
                textMessage = stringResource(
                    when (mode) {
                        VoiceSearchMode.CONTACT -> R.string.voice_search_empty_message_contact
                        VoiceSearchMode.FACILITY -> R.string.voice_search_empty_message_facility
                        VoiceSearchMode.ISLAND -> R.string.voice_search_empty_message_island
                    },
                    query
                ),
                iconImageVector = Icons.Default.PersonSearch
            )

            else -> Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.voice_search_disambiguation_message, query),
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn {
                    when (mode) {
                        VoiceSearchMode.CONTACT -> items(uiState.clientResults) { client ->
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onNavigateToContactList(client.id, client.companyName)
                                    }
                            ) {
                                ListItem(headlineContent = { Text(client.companyName) })
                            }
                        }

                        VoiceSearchMode.FACILITY -> items(uiState.facilityResults) { facility ->
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onNavigateToFacilityDetail(facility.clientId, facility.id)
                                    }
                            ) {
                                ListItem(
                                    headlineContent = { Text(facility.displayName) },
                                    supportingContent = facility.addressDisplay?.let { addr ->
                                        { Text(addr) }
                                    }
                                )
                            }
                        }

                        VoiceSearchMode.ISLAND -> items(uiState.islandResults) { island ->
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        onNavigateToIslandDetail(island.facilityId, island.id)
                                    }
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(island.customName ?: island.serialNumber)
                                    },
                                    supportingContent = island.customName?.let {
                                        { Text(island.serialNumber) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
