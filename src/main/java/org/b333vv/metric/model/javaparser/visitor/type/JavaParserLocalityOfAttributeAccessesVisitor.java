package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaParserLocalityOfAttributeAccessesVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        long localMethods = 0;
        List<BodyDeclaration<?>> methodsAndConstructors = Stream.concat(
                n.getMethods().stream(),
                n.getConstructors().stream()
        ).collect(Collectors.toList());

        if (methodsAndConstructors.isEmpty()) {
            collector.accept(Metric.of(MetricType.LAA, Value.of(1.0)));
            return;
        }
        try {
            String currentClassName = n.resolve().getQualifiedName();
            for (BodyDeclaration<?> methodOrConstructor : methodsAndConstructors) {
                List<FieldAccessExpr> foreignAccesses = new ArrayList<>();
                methodOrConstructor.walk(FieldAccessExpr.class, fae -> {
                    String declaringClassName = fae.resolve().asField().declaringType().getQualifiedName();
                    if (!declaringClassName.equals(currentClassName)) {
                        foreignAccesses.add(fae);
                    }
                });
                if (foreignAccesses.isEmpty()) {
                    localMethods++;
                }
            }
            double laa = (double) localMethods / methodsAndConstructors.size();
            collector.accept(Metric.of(MetricType.LAA, Value.of(laa)));
        } catch (Exception e) {
            collector.accept(Metric.of(MetricType.LAA, Value.UNDEFINED));
        }
    }
}
