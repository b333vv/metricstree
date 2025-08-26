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
        n.walk(ClassOrInterfaceType.class, t -> {
            try {
                coupledClasses.add(t.resolve().asReferenceType().getQualifiedName());
            } catch (Exception e) {
                // Unsolved symbol
            }
        });

        // The current class itself will be in the set, remove it.
        try {
            coupledClasses.remove(n.resolve().getQualifiedName());
        } catch (Exception e) {
            // ignore
        }

        Metric metric = Metric.of(MetricType.CBO, Value.of(coupledClasses.size()));
        collector.accept(metric);
    }
}
