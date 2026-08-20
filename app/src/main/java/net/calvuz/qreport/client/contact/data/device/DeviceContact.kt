package net.calvuz.qreport.client.contact.data.device

/**
 * Dati di un contatto letti dalla rubrica del telefono (picker) o
 * da un vCard condiviso — trasferimento dati una tantum, nessun
 * collegamento persistente con [net.calvuz.qreport.client.contact.domain.model.Contact].
 */
data class DeviceContact(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val mobilePhone: String? = null,
    val email: String? = null,
    val company: String? = null
)
