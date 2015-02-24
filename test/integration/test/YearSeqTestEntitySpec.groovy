package test

import grails.test.mixin.TestFor
import grails.util.Holders
import org.joda.time.LocalDate
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(YearSeqTestEntity)
class YearSeqTestEntitySpec extends Specification {

    def setup() {
        // the following are set in config
//        Holders.config.sequence.YearSeqTestEntity.code = "TEST"
//        Holders.config.sequence.YearSeqTestEntity.format = "%04d" // this is for padding seq nbr only
    }

    def cleanup() {
    }

    void "test something"() {
        when:
        LocalDate.metaClass.static.now = { -> new LocalDate(2014, 1, 1) }
        YearSeqTestEntity entity = new YearSeqTestEntity()
        then:
        entity.respondsTo("beforeValidate")
        and:
        entity.metaClass.respondsTo(entity, 'getNextSequenceNumber')
        and:
        entity.getNextSequenceNumber() == "TEST20140001"
        and:
        entity.getNextSequenceNumber() == "TEST20140002"
        and:
        entity.getNextSequenceNumber() == "TEST20140003"

        when:
        LocalDate.metaClass.static.now = { -> new LocalDate(2015, 1, 1) }
        then:
        entity.getNextSequenceNumber() == "TEST20150001"

        when:
        YearSeqTestEntity entity1 = new YearSeqTestEntity(name: "entity01")
        and:
        entity1.save(flush: true)
        then:
        entity1.code == "TEST20150002"
    }
}
