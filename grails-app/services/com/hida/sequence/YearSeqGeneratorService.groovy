package com.hida.sequence

import grails.transaction.Transactional
import grails.util.Holders
import org.joda.time.LocalDate

@Transactional
class YearSeqGeneratorService {
    def sequenceGeneratorService
    private static final DEFAULT_FORMATTER = Holders.config.sequence?.defaultFormatter ?: {
        String nm, String grp, Long tnt, String seq ->
        String nameCd = Holders.config.sequence?."$nm"?.code
        "${nameCd ?: ''}${grp ?: ''}${tnt ?: ''}${seq}"
    }
    def nextNumber(String name, Long tenantId = null) {
        LocalDate now = LocalDate.now()
        String yearGroup = Holders.config.sequence?."$name"?.monthly ? "${now.year}-${now.monthOfYear}" : "${now.year}"
        String seqNbr = sequenceGeneratorService.nextNumber(name, yearGroup, tenantId)
        Closure formatter = Holders.config.sequence?."$name"?.formatterClosure ?: DEFAULT_FORMATTER
        return formatter.call(name, yearGroup, tenantId, seqNbr)
    }
}
