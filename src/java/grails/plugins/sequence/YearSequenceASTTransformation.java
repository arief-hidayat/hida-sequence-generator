package grails.plugins.sequence;

import groovy.lang.ExpandoMetaClass;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;

/**
 * Created by hida on 24/2/2015.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class YearSequenceASTTransformation implements ASTTransformation {

    //private static final Log LOG = LogFactory.getLog(YearSequenceASTTransformation.class);

    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        ExpandoMetaClass.disableGlobally();

        for (ASTNode astNode : nodes) {
            if (astNode instanceof ClassNode) {
                ClassNode theClass = (ClassNode) astNode;
                AnnotationNode sequenceDefinition = GrailsASTUtils.findAnnotation(ClassHelper.make(YearSequenceEntity.class), theClass.getAnnotations());

                Expression propertyExpr = sequenceDefinition.getMember("property");
                if(propertyExpr == null) {
                    propertyExpr = new ConstantExpression("code");
                }
                String propertyName = propertyExpr.getText();

                if (!GrailsASTUtils.hasOrInheritsProperty(theClass, propertyName)) {
                    System.out.println("Adding sequence field [" + propertyName + "] to class " + theClass.getName());

                    Expression maxSize = sequenceDefinition.getMember("maxSize");
                    Expression blank = sequenceDefinition.getMember("blank");
                    Expression unique = sequenceDefinition.getMember("unique");
                    if(unique != null) {
                        String uniqueText = unique.getText();
                        if("true".equalsIgnoreCase(uniqueText)) {
                            unique = ConstantExpression.TRUE;
                        } else if("false".equalsIgnoreCase(uniqueText)) {
                            unique = ConstantExpression.FALSE;
                        } else {
                            unique = new ConstantExpression(uniqueText);
                        }
                    }
                    theClass.addProperty(propertyName, Modifier.PUBLIC, ClassHelper.STRING_TYPE, null, null, null);
                    Statement numberConstraintExpression = createStringConstraint(propertyExpr, maxSize, blank, unique);

                    PropertyNode constraints = theClass.getProperty("constraints");
                    if (constraints != null) {
                        if (constraints.getInitialExpression() instanceof ClosureExpression) {
                            ClosureExpression ce = (ClosureExpression) constraints.getInitialExpression();
                            ((BlockStatement) ce.getCode()).addStatement(numberConstraintExpression);
                        } else {
                            System.err.println("Do not know how to add constraints expression to non ClosureExpression " + constraints.getInitialExpression());
                        }
                    } else {
                        Statement[] constraintsStatement = {numberConstraintExpression};
                        BlockStatement closureBlock = new BlockStatement(constraintsStatement, null);
                        ClosureExpression constraintsClosure = new ClosureExpression(null, closureBlock);
                        theClass.addProperty("constraints", Modifier.STATIC | Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, constraintsClosure, null, null);
                    }
                }

                BeforeValidateInjection.generate(theClass, propertyName);

                VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit);
                scopeVisitor.visitClass(theClass);
            }
        }

        ExpandoMetaClass.enableGlobally();
    }

    private Statement createStringConstraint(Expression propertyName, Expression maxSize, Expression blank, Expression unique) {
        NamedArgumentListExpression nale = new NamedArgumentListExpression();
        if(maxSize != null) {
            nale.addMapEntryExpression(new MapEntryExpression(new ConstantExpression("maxSize"), maxSize));
        }
        if(blank != null) {
            nale.addMapEntryExpression(new MapEntryExpression(new ConstantExpression("blank"), blank));
        }
        if(unique != null) {
            nale.addMapEntryExpression(new MapEntryExpression(new ConstantExpression("unique"), unique));
        }

        MethodCallExpression mce = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, propertyName, nale);
        return new ExpressionStatement(mce);
    }
}
