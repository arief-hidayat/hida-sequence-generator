package test

import grails.plugins.sequence.YearSequenceEntity

@YearSequenceEntity(property = "code")
class YearSeqTestEntity {
    String code // optional
    String name

    String toString() {
        "#$code $name"
    }
}
