package net.calvuz.qreport.checkup.items.domain.usecase

import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.checkup.items.domain.model.CheckItemTemplateMaster
import net.calvuz.qreport.checkup.items.domain.repository.CheckItemTemplateMasterRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Updates an existing checklist template after validating required fields.
 *
 * [template.id] is editable from the form (it doubles as the "codice" shown in
 * reports, see [CheckItemTemplateMaster]) — if it differs from [originalId], this
 * is a rename (delete + insert under the new id), guarded by a uniqueness check.
 */
class UpdateCheckItemTemplateUseCase @Inject constructor(
    private val repository: CheckItemTemplateMasterRepository
) {
    suspend operator fun invoke(originalId: String, template: CheckItemTemplateMaster): QrResult<Unit, QrError> {

        if (template.id.isBlank() || template.category.isBlank() || template.description.isBlank() ||
            template.moduleTypeId.isBlank() || template.criticalityId.isBlank()
        ) {
            return QrResult.Error(QrError.ValidationError.EmptyField())
        }

        if (template.id != originalId) {
            if (repository.getTemplateById(template.id) != null) {
                return QrResult.Error(QrError.ValidationError.DuplicateId())
            }
            return repository.renameTemplate(originalId, template).fold(
                onSuccess = { QrResult.Success(Unit) },
                onFailure = {
                    Timber.e(it, "Failed to rename check item template $originalId -> ${template.id}")
                    QrResult.Error(QrError.App.SaveError())
                }
            )
        }

        return repository.updateTemplate(template).fold(
            onSuccess = { QrResult.Success(Unit) },
            onFailure = {
                Timber.e(it, "Failed to update check item template ${template.id}")
                QrResult.Error(QrError.App.SaveError())
            }
        )
    }
}
