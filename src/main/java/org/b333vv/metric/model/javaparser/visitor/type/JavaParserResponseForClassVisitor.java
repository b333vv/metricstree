package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserResponseForClassVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> declaredMethods = new HashSet<>();
        n.getMethods().forEach(method -> {
            try {
                declaredMethods.add(method.resolve().getQualifiedSignature());
            } catch (Exception e) {
                // Unsolved symbol
            }
        });

        Set<String> externalCalls = new HashSet<>();
        n.walk(MethodCallExpr.class, mce -> {
            try {
                if (mce.resolve().declaringType().getQualifiedName().equals(n.resolve().getQualifiedName())) {
                    // Method call is to a method within the same class
                } else {
                    externalCalls.add(mce.resolve().getQualifiedSignature());
                }
            } catch (Exception e) {
                // Unsolved symbol
            }
        });

        Metric metric = Metric.of(MetricType.RFC, Value.of(declaredMethods.size() + externalCalls.size()));
        collector.accept(metric);
    }
}
