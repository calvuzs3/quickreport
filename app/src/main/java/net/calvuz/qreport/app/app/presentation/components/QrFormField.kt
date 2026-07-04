package net.calvuz.qreport.app.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Campo testo condiviso per i form dell'app — sostituisce le varie
 * `OutlinedTextField` ad hoc (5 idiomi diversi di colore/errore trovati
 * nell'audit fase 3). `errorText` non nullo => `isError=true` e testo di
 * supporto nel colore d'errore del tema, sempre, in ogni form.
 *
 * La logica che decide se un campo è invalido resta nel ViewModel/validator
 * di ciascun modulo — questo componente centralizza solo la presentazione.
 */
@Composable
fun QrFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder?.let { { Text(it) } },
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
        keyboardOptions = keyboardOptions
    )
}
