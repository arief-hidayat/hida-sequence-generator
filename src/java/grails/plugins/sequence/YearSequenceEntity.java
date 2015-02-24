package grails.plugins.sequence;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass("grails.plugins.sequence.YearSequenceASTTransformation")
public @interface YearSequenceEntity {
    String property() default "code"; // here is the different
    int maxSize() default 20; // another different
    boolean blank() default false;
    String unique() default "true";
}
