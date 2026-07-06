package net.calvuz.qreport.checkup.items.domain.usecase

import net.calvuz.qreport.app.error.domain.model.QrError
import net.calvuz.qreport.app.result.domain.QrResult
import net.calvuz.qreport.checkup.items.domain.model.CheckItemTemplateMaster
import net.calvuz.qreport.checkup.items.domain.repository.CheckItemTemplateMasterRepository
import timber.log.Timber
import javax.inject.Inject

/**
 * Creates a new checklist template after validating required fields.
 *
 * [template.id] is user-editable (it doubles as the "codice" shown in reports,
 * see [CheckItemTemplateMaster]) — since [CheckItemTemplateMasterRepositoryImpl]
 * inserts with `OnConflictStrategy.REPLACE`, a colliding id would silently
 * overwrite an unrelated existing template, so uniqueness is checked here.
 */
class CreateCheckItemTemplateUseCase @Inject constructor(
    private val repository: CheckItemTemplateMasterRepository
) {
    suspend operator fun invoke(template: CheckItemTemplateMaster): QrResult<Unit, QrError> {

        if (template.id.isBlank() || template.category.isBlank() || template.description.isBlank() ||
            template.moduleTypeId.isBlank() || template.criticalityId.isBlank()
        ) {
            return QrResult.Error(QrError.ValidationError.EmptyField())
        }

        if (repository.getTemplateById(template.id) != null) {
            return QrResult.Error(QrError.ValidationError.DuplicateId())
        }

        return repository.createTemplate(template).fold(
            onSuccess = { QrResult.Success(Unit) },
            onFailure = {
                Timber.e(it, "Failed to create check item template ${template.id}")
                QrResult.Error(QrError.App.SaveError())
            }
        )
    }
}
