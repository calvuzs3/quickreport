package net.calvuz.qreport.client.contact.data.device

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Contacts
import timber.log.Timber

/**
 * Legge i dati di un contatto scelto tramite il picker di sistema
 * ([androidx.activity.result.contract.ActivityResultContracts.PickContact]).
 *
 * Richiede il permesso runtime READ_CONTACTS: il permesso di lettura temporaneo
 * che il picker concede sull'URI del contatto scelto non copre in modo affidabile
 * su tutti i device/versioni la sotto-query dei dati (nome, telefoni, email) —
 * verificato che senza il permesso la query fallisce silenziosamente su device reali.
 */
object DeviceContactReader {

    fun readFromUri(context: Context, contactUri: Uri): DeviceContact? {
        val resolver = context.contentResolver

        val displayName = resolver.query(
            contactUri,
            arrayOf(Contacts.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

        var firstName: String? = null
        var lastName: String? = null
        var phone: String? = null
        var mobilePhone: String? = null
        var email: String? = null
        var company: String? = null

        val dataUri = Uri.withAppendedPath(contactUri, Contacts.Data.CONTENT_DIRECTORY)
        try {
            resolver.query(
                dataUri,
                arrayOf(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.Data.DATA1,
                    ContactsContract.Data.DATA2,
                    ContactsContract.Data.DATA3
                ),
                null, null, null
            )?.use { cursor ->
                val mimeIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
                val data1Idx = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
                val data2Idx = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA2)
                val data3Idx = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA3)

                while (cursor.moveToNext()) {
                    when (cursor.getString(mimeIdx)) {
                        StructuredName.CONTENT_ITEM_TYPE -> {
                            firstName = cursor.getString(data2Idx) ?: firstName
                            lastName = cursor.getString(data3Idx) ?: lastName
                        }

                        Phone.CONTENT_ITEM_TYPE -> {
                            val number = cursor.getString(data1Idx)
                            val type = cursor.getInt(data2Idx)
                            if (!number.isNullOrBlank()) {
                                if (type == Phone.TYPE_MOBILE && mobilePhone == null) {
                                    mobilePhone = number
                                } else if (phone == null) {
                                    phone = number
                                }
                            }
                        }

                        Email.CONTENT_ITEM_TYPE -> {
                            if (email == null) email = cursor.getString(data1Idx)
                        }

                        Organization.CONTENT_ITEM_TYPE -> {
                            if (company == null) company = cursor.getString(data1Idx)
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Timber.e(e, "DeviceContactReader: permesso negato leggendo i dati del contatto")
            return null
        }

        if (firstName.isNullOrBlank() && !displayName.isNullOrBlank()) {
            val parts = displayName.trim().split(Regex("\\s+"), limit = 2)
            firstName = parts.getOrNull(0)
            lastName = lastName ?: parts.getOrNull(1)
        }

        if (firstName.isNullOrBlank()) return null

        return DeviceContact(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            mobilePhone = mobilePhone,
            email = email,
            company = company
        )
    }
}
