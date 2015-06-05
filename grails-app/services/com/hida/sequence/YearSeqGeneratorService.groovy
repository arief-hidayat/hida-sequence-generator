package com.hida.sequence

import grails.transaction.Transactional
import grails.util.Holders
import org.joda.time.LocalDate

@Transactional
class YearSeqGeneratorService {
    def sequenceGeneratorService
    private static final DEFAULT_FORMATTER = Holders.config.sequence?.defaultFormatter ?: {
        String prefix, String grp, Long tnt, String seq ->
        "${prefix ?: ''}${grp ?: ''}${tnt ?: ''}${seq}"
    }
    private static final TIME_FORMATTER = Holders.config.sequence?.timeFormatter ?: null // pass LocalDate now, boolean monthly

    def nextNumber(def delegateInstance) {
        String name = delegateInstance.class.simpleName
        Closure prefixClosure = Holders.config.sequence?."$name"?.prefixClosure ?: null
        String prefix = prefixClosure ? prefixClosure(delegateInstance) : (Holders.config.sequence?."$name"?.prefix ?: Holders.config.sequence?."$name"?.code)

        LocalDate now = LocalDate.now()
        boolean resetMonthly = Holders.config.sequence?."$name"?.monthly ?: false
        String yearGroup = TIME_FORMATTER ? TIME_FORMATTER(now, resetMonthly) : (resetMonthly ? "${now.year}${"${now.monthOfYear}".padLeft(2, "0")}" : "${now.year}")

        Closure tenantClosure = Holders.config.sequence?."$name"?.tenantClosure ?: null
        Long tenantId = tenantClosure ? tenantClosure(delegateInstance) : (delegateInstance.respondsTo('getTenantId') ? delegateInstance.tenantId : null)

        Closure formatter = Holders.config.sequence?."$name"?.formatterClosure ?: DEFAULT_FORMATTER
        String nbr
        delegateInstance.class.withNewSession {
            Long seqNbr = sequenceGeneratorService.nextNumberLong(prefix, yearGroup, tenantId)
            String numberFormat = Holders.config.sequence?."$name"?.format ?: '%s'
            nbr = formatter.call(prefix, yearGroup, tenantId, String.format(numberFormat, seqNbr))
        }
        return nbr
    }
}
