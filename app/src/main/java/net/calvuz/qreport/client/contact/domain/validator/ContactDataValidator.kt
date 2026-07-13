package net.calvuz.qreport.client.contact.domain.validator

import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.client.contact.domain.model.Contact
import net.calvuz.qreport.shared.validation.ContactValidationRules
import javax.inject.Inject

/**
 * Contact validator
 */
class ContactDataValidator @Inject constructor() {

    operator fun invoke(contact: Contact): QrResult<Unit, QrError> {
        return when {
            contact.clientId.isBlank() ->
                QrResult.Error(QrError.ContactsError.MissingClientId())

            contact.firstName.isBlank() ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidContactNameLength())

            !ContactValidationRules.isFirstNameLengthValid(contact.firstName) ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidContactNameLength())

            (contact.lastName?.length ?: 0) > ContactValidationRules.MAX_LAST_NAME_LENGTH ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidContactLastNameLength())

            contact.email?.isNotBlank() == true && !isValidEmail(contact.email) ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidEmail())

            contact.phone?.isNotBlank() == true && !isValidPhone(contact.phone) ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidPhone())

            contact.mobilePhone?.isNotBlank() == true && !isValidPhone(contact.mobilePhone) ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidMobile())

            (contact.title?.length ?: 0) > ContactValidationRules.MAX_TITLE_LENGTH ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidTitleLength())

            (contact.role?.length ?: 0) > ContactValidationRules.MAX_ROLE_LENGTH ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidRoleLength())

            (contact.department?.length ?: 0) > ContactValidationRules.MAX_DEPARTMENT_LENGTH ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidDepartmentLength())

            !hasAnyContactInfo(contact) ->
                QrResult.Error(QrError.ContactsError.ValidationError.InvalidContactInfo())

            else -> QrResult.Success(Unit)
        }
    }

    fun isValidEmail(email: String): Boolean = ContactValidationRules.isEmailValid(email)

    fun isValidPhone(phone: String): Boolean = ContactValidationRules.isPhoneValid(phone)

    /**
     * Check for at least one contact method
     */
    fun hasAnyContactInfo(contact: Contact): Boolean =
        ContactValidationRules.hasAnyContactInfo(contact.email, contact.phone, contact.mobilePhone)
}