package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserForeignDataProvidersVisitor extends JavaParserClassVisitor {
    private final List<ClassOrInterfaceDeclaration> allClasses;

    public JavaParserForeignDataProvidersVisitor(List<ClassOrInterfaceDeclaration> allClasses) {
        this.allClasses = allClasses;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> fdp = new HashSet<>();
        try {
            String currentClassName = n.resolve().getQualifiedName();
            for (ClassOrInterfaceDeclaration otherClass : allClasses) {
                if (otherClass.resolve().getQualifiedName().equals(currentClassName)) {
                    continue;
                }
                otherClass.walk(FieldAccessExpr.class, fae -> {
                    try {
                        if (fae.resolve().asField().declaringType().getQualifiedName().equals(currentClassName)) {
                            fdp.add(otherClass.resolve().getQualifiedName());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                });
            }
        } catch (Exception e) {
            // ignore
        }
        Metric metric = Metric.of(MetricType.FDP, Value.of(fdp.size()));
        collector.accept(metric);
    }
}
