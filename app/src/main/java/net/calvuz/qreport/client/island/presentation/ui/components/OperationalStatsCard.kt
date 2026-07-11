package net.calvuz.qreport.client.island.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.calvuz.qreport.R
import net.calvuz.qreport.app.app.presentation.components.QReportCard
import net.calvuz.qreport.app.app.presentation.components.QrListStatItem
import net.calvuz.qreport.app.app.presentation.components.StatItemOrientation
import net.calvuz.qreport.app.app.presentation.ui.theme.Spacing
import net.calvuz.qreport.app.util.SizeUtils.getFormattedCycleCount
import net.calvuz.qreport.client.island.domain.usecase.SingleIslandStatistics

/**
 * Riga "stat readout 4-up": stessa convenzione di Home e CheckUpDetailScreen —
 * etichetta di sezione + card individuali bordate con mira neutra, non un'unica
 * card con divisore (vedi design/design-system.md).
 */
@Composable
fun OperationalStatsCard(statistics: SingleIslandStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(R.string.island_stats_card_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            OperationalStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                value = stringResource(R.string.island_stats_hours_value, statistics.operationalStats.operatingHours),
                label = stringResource(R.string.island_stats_hours_label)
            )
            OperationalStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Repeat,
                value = statistics.operationalStats.cycleCount.getFormattedCycleCount(),
                label = stringResource(R.string.island_stats_cycles_label)
            )
            OperationalStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Default.TrendingUp,
                value = stringResource(R.string.island_stats_uptime_value, statistics.operationalStats.uptime),
                label = stringResource(R.string.island_stats_uptime_label)
            )
            OperationalStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Default.Assignment,
                value = stringResource(R.string.island_stats_checkups_value, statistics.totalCheckUps),
                label = stringResource(R.string.island_stats_checkups_label)
            )
        }
    }
}

@Composable
private fun OperationalStatTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    QReportCard(modifier = modifier, tickColor = MaterialTheme.colorScheme.outline) {
        QrListStatItem(
            icon = icon,
            value = value,
            label = label,
            orientation = StatItemOrientation.Vertical,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
        )
    }
}
