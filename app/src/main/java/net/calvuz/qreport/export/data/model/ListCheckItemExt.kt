package net.calvuz.qreport.export.data.model

import net.calvuz.qreport.checkup.items.domain.model.CheckItem

private val UUID_REGEX = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

/**
 * [CheckItem.itemCode], ma senza mai far trapelare un UUID grezzo nei report.
 * Poteva succedere per i check-up creati da un template prima che
 * `CheckItemTemplateMaster` avesse un campo `code` dedicato — in quel caso
 * `itemCode` veniva impostato a `template.id` (vedi `CreateCheckUpUseCase`).
 * Vuota se non c'è un codice leggibile: il chiamante decide il fallback
 * (es. omettere la riga, o usare la descrizione).
 */
val CheckItem.displayCode: String
    get() = if (UUID_REGEX.matches(itemCode)) "" else itemCode

fun List<CheckItem>.groupByModuleTypeId(): Map<String, List<CheckItem>> {
    return this.groupBy { it.moduleTypeId }
        .toSortedMap()
}

fun List<CheckItem>.getAllPhotos() = this.flatMap { it.photos }

fun Map<String, List<CheckItem>>.getPhotoCountByModule(): Map<String, Int> {
    return this.mapValues { (_, items) -> items.getAllPhotos().size }
}
