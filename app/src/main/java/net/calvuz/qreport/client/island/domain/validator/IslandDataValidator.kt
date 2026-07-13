package net.calvuz.qreport.client.island.domain.validator

import kotlinx.datetime.Clock
import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.client.island.domain.model.Island
import net.calvuz.qreport.shared.validation.IslandValidationRules
import javax.inject.Inject

/**
 * Validates Island data before create/update operations.
 *
 * [invoke]         — structural field validation (required fields, lengths, formats)
 * [validateDates]  — date consistency checks (installation, warranty, maintenance)
 *
 * Returns [QrResult.Success(Unit)] when valid.
 * Returns a typed [QrError.IslandError] when invalid — the presentation layer
 * resolves the user-facing string via QrErrorExt.toUiText().
 *
 * Error messages in data class constructors are English technical descriptions
 * for logging only; they never reach the UI directly.
 */
class IslandDataValidator @Inject constructor() {

    operator fun invoke(island: Island): QrResult<Unit, QrError.IslandError> = when {
        island.facilityId.isBlank() ->
            QrResult.Error(QrError.IslandError.MissingFacilityId())

        island.serialNumber.isBlank() ->
            QrResult.Error(QrError.IslandError.MissingSerialNumber())

        !IslandValidationRules.isSerialNumberLengthValid(island.serialNumber) ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidSerialNumberLength())

        !IslandValidationRules.isValidCode(island.serialNumber) ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidSerialNumber())

        island.commissioningNumber != null && !IslandValidationRules.isValidCode(island.commissioningNumber) ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidCommissioningNumber())

        (island.customName?.length ?: 0) > IslandValidationRules.MAX_CUSTOM_NAME_LENGTH ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidCustomNameLength())

        (island.location?.length ?: 0) > IslandValidationRules.MAX_LOCATION_LENGTH ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidLocationLength())

        !IslandValidationRules.isOperatingHoursValid(island.operatingHours) ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidOperatingHours())

        !IslandValidationRules.isCycleCountValid(island.cycleCount) ->
            QrResult.Error(QrError.IslandError.ValidationError.InvalidCycleCount())

        else -> QrResult.Success(Unit)
    }

    /**
     * Validates date consistency across installation, warranty and maintenance fields.
     * Returns null when all dates are consistent (null = valid, no error).
     * Called after [invoke] succeeds, both in create and update flows.
     */
    fun validateDates(island: Island): QrResult<Unit, QrError.IslandError>? {
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val installationMs = island.installationDate?.toEpochMilliseconds()
        val warrantyMs = island.warrantyExpiration?.toEpochMilliseconds()
        val lastMaintenanceMs = island.lastMaintenanceDate?.toEpochMilliseconds()
        val nextMaintenanceMs = island.nextScheduledMaintenance?.toEpochMilliseconds()

        return when {
            !IslandValidationRules.isInstallationDateValid(installationMs, nowMs) ->
                QrResult.Error(QrError.IslandError.ValidationError.InvalidInstallationDate())

            !IslandValidationRules.isWarrantyDateValid(warrantyMs, installationMs) ->
                QrResult.Error(QrError.IslandError.ValidationError.InvalidWarrantyDate())

            !IslandValidationRules.isMaintenanceDateValid(lastMaintenanceMs, installationMs, nowMs) ->
                QrResult.Error(QrError.IslandError.ValidationError.InvalidMaintenanceDate())

            !IslandValidationRules.isNextMaintenanceValid(nextMaintenanceMs, lastMaintenanceMs) ->
                QrResult.Error(QrError.IslandError.ValidationError.InvalidMaintenanceDate())

            else -> null
        }
    }
}