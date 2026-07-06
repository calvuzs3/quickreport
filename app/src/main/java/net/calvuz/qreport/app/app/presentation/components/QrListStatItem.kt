package net.calvuz.qreport.app.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Layout dello stat item: [Horizontal] icona-valore-etichetta in riga (default,
 * comportamento invariato per i 26 call site esistenti), [Vertical] icona sopra,
 * valore ed etichetta impilati sotto (per i tile di dettaglio più grandi).
 */
enum class StatItemOrientation { Horizontal, Vertical }

@Composable
fun QrListStatItem(
    icon: ImageVector? = null,
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    // Se non null, avvolge il contenuto in una Surface piena (pillola/chip) con
    // questo colore di sfondo, invece del rendering trasparente di default.
    containerColor: Color? = null,
    orientation: StatItemOrientation = StatItemOrientation.Horizontal,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (containerColor != null) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.small,
            color = containerColor
        ) {
            StatItemBody(
                icon = icon,
                value = value,
                label = label,
                color = color,
                orientation = orientation,
                compact = compact,
                chip = true,
                bodyModifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    } else {
        StatItemBody(
            icon = icon,
            value = value,
            label = label,
            color = color,
            orientation = orientation,
            compact = compact,
            chip = false,
            bodyModifier = modifier
        )
    }
}

@Composable
private fun StatItemBody(
    icon: ImageVector?,
    value: String,
    label: String,
    color: Color,
    orientation: StatItemOrientation,
    compact: Boolean,
    chip: Boolean,
    bodyModifier: Modifier
) {
    when (orientation) {
        StatItemOrientation.Vertical -> Column(
            modifier = bodyModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        StatItemOrientation.Horizontal -> Row(
            modifier = bodyModifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 14.dp else 16.dp),
                    tint = color
                )
            }
            if (chip) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.85f)
                    )
                }
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
                if (label.isNotEmpty() && !compact) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
        }
    }
}
