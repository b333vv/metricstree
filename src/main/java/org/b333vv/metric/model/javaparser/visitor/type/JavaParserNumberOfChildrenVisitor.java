package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.List;
import java.util.function.Consumer;

public class JavaParserNumberOfChildrenVisitor extends JavaParserClassVisitor {
    private final List<ClassOrInterfaceDeclaration> allClasses;

    public JavaParserNumberOfChildrenVisitor(List<ClassOrInterfaceDeclaration> allClasses) {
        this.allClasses = allClasses;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        super.visit(n, collector);
        try {
            String currentClassQualifiedName = n.resolve().getQualifiedName();
            long numberOfChildren = allClasses.stream()
                    .filter(c -> c.getExtendedTypes().stream()
                            .anyMatch(et -> {
                                try {
                                    return et.resolve().asReferenceType().getQualifiedName().equals(currentClassQualifiedName);
                                } catch (Exception e) {
                                    return false;
                                }
                            }))
                    .count();
            Metric metric = Metric.of(MetricType.NOC, Value.of(numberOfChildren));
            collector.accept(metric);
        } catch (Exception e) {
            Metric metric = Metric.of(MetricType.NOC, Value.UNDEFINED);
            collector.accept(metric);
        }
    }
}
