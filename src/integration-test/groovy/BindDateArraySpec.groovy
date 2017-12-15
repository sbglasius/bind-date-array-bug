import grails.databinding.SimpleMapDataBindingSource
import grails.persistence.Entity
import grails.testing.mixin.integration.Integration
import grails.web.databinding.GrailsWebDataBinder
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

@Integration
class BindDateArraySpec extends Specification {
    GrailsWebDataBinder grailsWebDataBinder
    private HttpServletRequest request = new MockHttpServletRequest()
    private GrailsParameterMap params = new GrailsParameterMap(request)

    void 'can bind to a top level field'() {
        given:
            params.putAll([
                    name          : 'Alex',
                    birthday      : 'struct',
                    birthday_day  : '02',
                    birthday_month: '10',
                    birthday_year : '2008'
            ])

        and:
            def person = new Person()

        when:
            grailsWebDataBinder.bind person, params as SimpleMapDataBindingSource

        then:
            person.name == 'Alex'
            person.birthday == Date.parse('yyyy-MM-d', "2008-10-2")
    }

    void 'can bind to a child level field'() {
        given:
            params.putAll([
                    name                  : 'Rob',
                    'child.name'          : 'Alex',
                    'child.birthday'      : 'struct',
                    'child.birthday_day'  : '02',
                    'child.birthday_month': '10',
                    'child.birthday_year' : '2008'
            ])

        and:
            def person = new Person()

        when:
            grailsWebDataBinder.bind person, params as SimpleMapDataBindingSource

        then:
            person.name == 'Rob'
            person.birthday == null
            person.child.name == 'Alex'
            person.child.birthday == Date.parse('yyyy-MM-d', "2008-10-2")
    }

    void 'can bind collection (not date)'() {
        given:
            params.putAll([
                    'givenName'                : 'Joy',
                    'members[0].name'          : 'Alex',
                    'members[0].birthday'      : 'struct',
                    'members[0].birthday_day'  : '02',
                    'members[0].birthday_month': '10',
                    'members[0].birthday_year' : '2008',
                    'members[1].name'          : 'Alexandra',
                    'members[1].birthday'      : 'struct',
                    'members[1].birthday_day'  : '01',
                    'members[1].birthday_month': '12',
                    'members[1].birthday_year' : '2009'
            ])

        and:
            Family family = new Family()

        when:
            grailsWebDataBinder.bind family, params as SimpleMapDataBindingSource

        then:
            family.givenName == 'Joy'
            family.members.size() == 2
            family.members[0].name == 'Alex'
            family.members[0].birthday == Date.parse('yyyy-MM-d', "2008-10-2")
            family.members[1].name == 'Alexandra'
            family.members[1].birthday == Date.parse('yyyy-MM-d', "2009-12-1")
    }

    void 'can bind to a collection field'() {
        given:
            params.putAll([
                    'name'          : 'Groovy & Grails User Group',
                    'dates[0]'      : 'struct',
                    'dates[0]_day'  : '09',
                    'dates[0]_month': '11',
                    'dates[0]_year' : '2012',
                    'dates[1]'      : 'struct',
                    'dates[1]_day'  : '13',
                    'dates[1]_month': '12',
                    'dates[1]_year' : '2012',
            ])

        and:
            def event = new RecurringEvent()

        when:
            grailsWebDataBinder.bind(event, params as SimpleMapDataBindingSource)

        then:
            !event.errors.hasErrors()

        and:
            event.name == params.name
            event.dates == [Date.parse('yyyy-MM-d', "2012-11-9"), Date.parse('yyyy-MM-d', "2012-12-13")]
    }

}

@Entity
class Person {
    String name
    Date birthday
    Person child
}

@Entity
class Family {
    String givenName
    List<Person> members
    static hasMany = [members: Person]
}

@Entity
class RecurringEvent {
    String name
    List<Date> dates
    static hasMany = [dates: Date]
}
