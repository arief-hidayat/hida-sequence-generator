package com.hida.sequence

import grails.plugins.sequence.SequenceGenerator
import grails.plugins.sequence.SequenceNumber
import grails.transaction.Transactional
import grails.util.Holders
import org.joda.time.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Transactional
class YearSeqGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(YearSeqGeneratorService.class)
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

    SequenceGenerator sequenceGenerator

    void recoverSequences(LocalDate now) {
        for(String domainName : Holders.config.sequence?.autoRecoverSequences ?: []) {
            recoverSequence(domainName, now)
        }
    }

    void recoverSequence(String domainName, LocalDate now) {
        def prConfig =  Holders.config.sequence?."$domainName"
        if(!prConfig.lastSeqNbrFromDomainClosure) return

        boolean resetMonthly = prConfig?.monthly ?: false
        String yearGroup = TIME_FORMATTER ? TIME_FORMATTER(now, resetMonthly) : (resetMonthly ? "${now.year}${"${now.monthOfYear}".padLeft(2, "0")}" : "${now.year}")


        Closure formatter = prConfig?.formatterClosure ?: SequenceGenInput.DEFAULT_FORMATTER

        for(SequenceNumber sequenceNumber : SequenceNumber.findAllWhere(group: yearGroup)) {
            String name = sequenceNumber.definition.name
            Long tenant = sequenceNumber.definition.tenantId
            String formatWithoutSeqNbr = formatter(name, yearGroup, tenant, "")
            Long seqNbr = prConfig.lastSeqNbrFromDomainClosure(formatWithoutSeqNbr)
            if(seqNbr && seqNbr + 1 != sequenceNumber.number) {
                sequenceGenerator.update(tenant, name, yearGroup, sequenceNumber.definition.format, sequenceNumber.number, seqNbr + 1)
                logger.info("Sync sequence number for ${formatWithoutSeqNbr}*. Previous Nbr is ${sequenceNumber.number}. Changed to ${seqNbr + 1}")
            }
        }
    }
}
