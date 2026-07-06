package net.calvuz.qreport.app.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Dropdown generico condiviso per i form — sostituisce i 9
 * `ExposedDropdownMenuBox` reimplementati da zero nell'audit fase 3.
 * A differenza di `QReportSelectorMenu`/`QReportSelectorRow` (pensati per i
 * filtri di lista, tipizzati su `QReportFilter`), questo è generico su
 * qualunque tipo `T` usato come opzione di un campo form.
 *
 * `optionContent`, se fornito, sostituisce il rendering di default (singola
 * riga con `optionLabel`) per le voci menu che hanno bisogno di più di una
 * riga (es. label + descrizione).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> QrDropdownField(
    selected: T,
    options: List<T>,
    label: String,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    optionContent: (@Composable (T) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { if (optionContent != null) optionContent(option) else Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
