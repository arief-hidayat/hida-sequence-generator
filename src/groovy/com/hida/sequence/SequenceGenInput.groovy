package com.hida.sequence

import grails.util.Holders

/**
 * Created by arief.hidayat on 15/06/2015.
 */
class SequenceGenInput {
    String name
    String prefix
    String group
    Long tenantId
    String seqNbrPaddingFormat
    Closure formatter
    public static final DEFAULT_FORMATTER = Holders.config.sequence?.defaultFormatter ?: {
        String nm, String grp, Long tnt, String seq ->
            return "${nm ?: ''}${grp ?: ''}${tnt ?: ''}${seq}"
    }

    public String format(Long seqNbr) {
        (formatter ?: DEFAULT_FORMATTER).call(this.prefix, this.group, this.tenantId, String.format(seqNbrPaddingFormat, seqNbr))
    }
}
