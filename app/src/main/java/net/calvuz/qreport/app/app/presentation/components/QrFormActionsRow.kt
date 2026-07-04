package net.calvuz.qreport.app.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.calvuz.qreport.R

/**
 * Riga Salva/Annulla condivisa per i form full-screen e i dialog custom
 * (non gli `AlertDialog`-based, che restano sui loro slot nativi
 * `confirmButton`/`dismissButton`) — sostituisce le 16 varianti ad hoc
 * trovate nell'audit fase 3.
 */
@Composable
fun QrFormActionsRow(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    isSaving: Boolean = false,
    cancelText: String = stringResource(R.string.action_cancel),
    saveText: String = stringResource(R.string.action_save)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isSaving
        ) {
            Text(cancelText)
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            enabled = saveEnabled && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(saveText)
            }
        }
    }
}
