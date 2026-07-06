package net.calvuz.qreport.app.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Wrapper condiviso per le card lista (Client/Facility/Contact/Island/
 * MechanicalUnit/Checkup/Contract/TechnicalIntervention/...), estratto dalle
 * `Card()` esterne — quasi identiche — di ciascuna. Vedi design/design-system.md.
 */
@Composable
fun QReportCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
    // Colore semantico della riga per le mire d'angolo (design/mockup-orange-technical.html,
    // classe .plate). null = nessuna mira, comportamento invariato.
    tickColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = (if (tickColor != null) modifier.cornerRegistrationMarks(tickColor) else modifier)
        .fillMaxWidth()
    val shape = RoundedCornerShape(9.dp) // via di mezzo tra i 12dp iniziali e i 7dp (troppo squadrati), richiesta esplicita dopo verifica su device
    // Design piatto (design/mockup-orange-technical.html: .comp-card/.nameplate/.stat
    // non hanno mai box-shadow, solo `border: 1px solid var(--border)`) — niente
    // ombra Material, la separazione dalla pagina la fa il bordo sottile.
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = BorderStroke(
        1.dp,
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    )
    // CardDefaults.cardColors() SENZA argomenti non usa colorScheme.surface:
    // la Card "filled" di Material3 di default prende containerColor dal
    // token FilledCardTokens.ContainerColor = surfaceContainerHighest, che
    // qui coincide col colore del bordo (outline) — bordo e sfondo identici,
    // il bordo spariva e la card sembrava un blocco grigio pieno. Va sempre
    // passato containerColor esplicito.
    val colors = if (isSelected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    }
    val innerContent: @Composable ColumnScope.() -> Unit = {
        content()
        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = cardModifier, shape = shape, elevation = elevation, colors = colors, border = border, content = innerContent)
    } else {
        Card(modifier = cardModifier, shape = shape, elevation = elevation, colors = colors, border = border, content = innerContent)
    }
}

/**
 * Motivo "mire d'angolo": due piccole "L" da 9×9dp negli angoli opposti
 * (alto-sinistra, basso-destra), richiamano i segni di registrazione di un
 * disegno tecnico. Vedi design/mockup-orange-technical.html, classe `.plate`.
 */
fun Modifier.cornerRegistrationMarks(
    color: Color,
    tickSize: Dp = 9.dp,
    strokeWidth: Dp = 2.dp
): Modifier = this.drawWithContent {
    drawContent()
    val tickPx = tickSize.toPx()
    val strokePx = strokeWidth.toPx()
    val markColor = color.copy(alpha = 0.5f)

    // Alto-sinistra
    drawLine(markColor, Offset(0f, strokePx / 2), Offset(tickPx, strokePx / 2), strokePx)
    drawLine(markColor, Offset(strokePx / 2, 0f), Offset(strokePx / 2, tickPx), strokePx)

    // Basso-destra
    drawLine(markColor, Offset(size.width - tickPx, size.height - strokePx / 2), Offset(size.width, size.height - strokePx / 2), strokePx)
    drawLine(markColor, Offset(size.width - strokePx / 2, size.height - tickPx), Offset(size.width - strokePx / 2, size.height), strokePx)
}
