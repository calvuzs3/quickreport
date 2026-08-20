package net.calvuz.qreport.client.contact.data.device

/**
 * Parser minimale per vCard 2.1/3.0 condivisi dall'app Contatti nativa
 * ("Condividi" → QuickReport). Copre solo i campi usati dal form Contact
 * (N/FN, TEL, EMAIL, ORG) — non è un parser RFC 6350 completo (niente foto,
 * indirizzi, campi custom X-).
 */
object VCardParser {

    fun parse(text: String): DeviceContact? {
        var firstName: String? = null
        var lastName: String? = null
        var fullName: String? = null
        var phone: String? = null
        var mobilePhone: String? = null
        var email: String? = null
        var company: String? = null

        for (line in unfold(text)) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val colonIdx = trimmed.indexOf(':')
            if (colonIdx < 0) continue

            val prefix = trimmed.substring(0, colonIdx)
            val value = unescape(trimmed.substring(colonIdx + 1)).trim()
            if (value.isEmpty()) continue

            val prefixParts = prefix.split(';')
            val propName = prefixParts[0].substringAfterLast('.').uppercase()
            val params = prefixParts.drop(1).map { it.uppercase() }

            when (propName) {
                "N" -> {
                    val components = value.split(';')
                    lastName = components.getOrNull(0)?.takeIf { it.isNotBlank() } ?: lastName
                    firstName = components.getOrNull(1)?.takeIf { it.isNotBlank() } ?: firstName
                }

                "FN" -> fullName = value

                "TEL" -> {
                    val isMobile = params.any { it.contains("CELL") || it.contains("MOBILE") }
                    if (isMobile) {
                        if (mobilePhone == null) mobilePhone = value
                    } else if (phone == null) {
                        phone = value
                    } else if (mobilePhone == null) {
                        mobilePhone = value
                    }
                }

                "EMAIL" -> if (email == null) email = value

                "ORG" -> if (company == null) {
                    company = value.split(';').firstOrNull { it.isNotBlank() }
                }
            }
        }

        if (firstName.isNullOrBlank() && !fullName.isNullOrBlank()) {
            val parts = fullName.trim().split(Regex("\\s+"), limit = 2)
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

    /** Ricongiunge le righe "foldate" (continuazione = riga che inizia con spazio/tab). */
    private fun unfold(text: String): List<String> {
        val result = mutableListOf<String>()
        for (line in text.split("\r\n", "\n", "\r")) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && result.isNotEmpty()) {
                result[result.lastIndex] = result.last() + line.substring(1)
            } else {
                result.add(line)
            }
        }
        return result
    }

    private fun unescape(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                when (value[i + 1]) {
                    'n', 'N' -> sb.append('\n')
                    else -> sb.append(value[i + 1])
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}
