package com.hida.sequence

import grails.transaction.Transactional
import grails.util.Holders
import org.joda.time.LocalDate

@Transactional
class YearSeqGeneratorService {
    def sequenceGeneratorService
    private static final TIME_FORMATTER = Holders.config.sequence?.timeFormatter ?: null // pass LocalDate now, boolean monthly

    def nextNumber(SequenceGenInput input) {
        Long seqNbr = sequenceGeneratorService.nextNumberLong(input.prefix, input.group, input.tenantId)
        return input.format(seqNbr)
    }

    SequenceGenInput getInput(String name, def delegateInstance) {
        Closure prefixClosure = Holders.config.sequence?."$name"?.prefixClosure ?: null
        String prefix = prefixClosure ? prefixClosure(delegateInstance) : (Holders.config.sequence?."$name"?.prefix ?: Holders.config.sequence?."$name"?.code)

        LocalDate now = LocalDate.now()
        boolean resetMonthly = Holders.config.sequence?."$name"?.monthly ?: false
        String yearGroup = TIME_FORMATTER ? TIME_FORMATTER(now, resetMonthly) : (resetMonthly ? "${now.year}${"${now.monthOfYear}".padLeft(2, "0")}" : "${now.year}")

        Closure tenantClosure = Holders.config.sequence?."$name"?.tenantClosure ?: null
        Long tenantId = tenantClosure ? tenantClosure(delegateInstance) : (delegateInstance.respondsTo('getTenantId') ? delegateInstance.tenantId : null)

        Closure formatter = Holders.config.sequence?."$name"?.formatterClosure ?: null

        String seqNbrPaddingFormat = Holders.config.sequence?."$name"?.format ?: '%s'

        new SequenceGenInput(name: name, prefix: prefix, group: yearGroup, tenantId: tenantId,
                seqNbrPaddingFormat : seqNbrPaddingFormat, formatter: formatter)
    }
}
