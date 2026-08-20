package net.calvuz.qreport.client.contact.data.device

/**
 * Passaggio one-shot di un [DeviceContact] ricevuto via "Condividi" dall'app
 * Contatti, dalla schermata di scelta cliente al [net.calvuz.qreport.client.contact.presentation.ui.ContactFormViewModel].
 * Evita di dover serializzare i dati del contatto in un nav-arg.
 */
object PendingImportedContactHolder {

    private var pending: DeviceContact? = null

    fun set(deviceContact: DeviceContact) {
        pending = deviceContact
    }

    /** Legge e consuma il valore in sospeso — una volta letto torna a essere `null`. */
    fun consume(): DeviceContact? {
        val value = pending
        pending = null
        return value
    }
}
