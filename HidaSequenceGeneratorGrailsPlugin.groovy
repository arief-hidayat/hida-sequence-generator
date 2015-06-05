import grails.plugins.sequence.YearSequenceEntity
import grails.util.Holders

class HidaSequenceGeneratorGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def loadAfter = ['domainClass', 'services']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/test/YearSeqTestEntity.groovy"
    ]

    // TODO Fill in these fields
    def title = "Hida Sequence Generator Plugin" // Headline display name of the plugin
    def author = "Arief Hidayat"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/hida-sequence-generator"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        for (c in application.domainClasses) {
            if (c.clazz.getAnnotation(YearSequenceEntity)) {
                addYearSeqDomainMethods(applicationContext, c.metaClass)
            }
        }
    }

    private void addYearSeqDomainMethods(ctx, MetaClass mc) {
        def service = ctx.getBean('yearSeqGeneratorService')
        mc.getNextSequenceNumber = { String group = null ->
//            def name = delegate.class.simpleName
//            Closure prefixClosure = Holders.config.sequence?."$name"?.prefixClosure ?: null
//            Closure tenantClosure = Holders.config.sequence?."$name"?.tenantClosure ?: null
//            def tenant = tenantClosure ? tenantClosure(delegate) : (delegate.respondsTo('getTenantId') ? delegate.tenantId : null)
//            def nbr
//            delegate.class.withNewSession {
////                if(prefixClosure) name = prefixClosure(delegate)
//                nbr = service.nextNumber(delegate)
//            }
//            return nbr
            // make sure the implementation use withNewSession
            return service.nextNumber(delegate)
        }
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
