package net.calvuz.qreport.client.contact.data.device

import android.content.Intent
import android.provider.ContactsContract
import net.calvuz.qreport.client.contact.domain.model.Contact

/**
 * Costruisce un [Intent] che apre l'editor nativo dei Contatti già precompilato
 * con i dati di [contact], lasciando all'utente la scelta dell'account
 * (Google o solo telefono) e la conferma del salvataggio.
 *
 * Nessun `WRITE_CONTACTS` richiesto: la scrittura è delegata interamente
 * all'app Contatti di sistema.
 */
object DeviceContactExportIntent {

    fun build(contact: Contact, clientName: String? = null): Intent {
        return Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE

            putExtra(
                ContactsContract.Intents.Insert.NAME,
                listOfNotNull(contact.firstName, contact.lastName)
                    .joinToString(" ")
                    .trim()
            )

            contact.mobilePhone?.takeIf { it.isNotBlank() }?.let {
                putExtra(ContactsContract.Intents.Insert.PHONE, it)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            }

            contact.phone?.takeIf { it.isNotBlank() }?.let {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, it)
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
            }

            (contact.email ?: contact.alternativeEmail)?.takeIf { it.isNotBlank() }?.let {
                putExtra(ContactsContract.Intents.Insert.EMAIL, it)
                putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
            }

            contact.role?.takeIf { it.isNotBlank() }?.let {
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it)
            }

            // Non esiste un extra dedicato al "dipartimento": usiamo il nome
            // cliente come azienda, che per un Contact (referente di un cliente)
            // è l'informazione più utile in rubrica.
            clientName?.takeIf { it.isNotBlank() }?.let {
                putExtra(ContactsContract.Intents.Insert.COMPANY, it)
            }
        }
    }
}
