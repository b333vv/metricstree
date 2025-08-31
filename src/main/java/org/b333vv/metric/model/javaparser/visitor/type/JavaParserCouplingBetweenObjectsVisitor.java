package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserCouplingBetweenObjectsVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> coupledClasses = new HashSet<>();
        
        // Find all types that this class depends on (outgoing dependencies)
        n.walk(ClassOrInterfaceType.class, t -> {
            try {
                String resolvedName = t.resolve().asReferenceType().getQualifiedName();
                coupledClasses.add(resolvedName);
            } catch (Exception e) {
                // Unsolved symbol - skip
            }
        });

        // Remove the current class itself from the coupled classes set
        try {
            String currentClassName = n.resolve().getQualifiedName();
            coupledClasses.remove(currentClassName);
        } catch (Exception e) {
            // If we can't resolve the current class name, that's fine
            // We'll just rely on it not being in the set anyway
        }
        
        Metric metric = Metric.of(MetricType.CBO, Value.of(coupledClasses.size()));
        collector.accept(metric);
    }
}
