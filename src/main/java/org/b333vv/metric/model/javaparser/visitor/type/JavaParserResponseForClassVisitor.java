package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserResponseForClassVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<ResolvedMethodDeclaration> allMethodsAndCalls = new HashSet<>();
        n.resolve().getAllMethods().forEach(method -> {
            try {
                allMethodsAndCalls.add(method.getDeclaration());
            } catch (Exception e) {
                // Unsolved symbol
            }
        });

        n.walk(MethodCallExpr.class, mce -> {
            try {
                allMethodsAndCalls.add(mce.resolve());
            } catch (Exception e) {
                // Unsolved symbol
            }
        });

        Metric metric = Metric.of(MetricType.RFC, Value.of(allMethodsAndCalls.size()));
        collector.accept(metric);
    }
}
