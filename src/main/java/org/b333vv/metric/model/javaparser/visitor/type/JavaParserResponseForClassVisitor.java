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
        long nom = n.getMethods().size();
        Set<String> calledMethods = new HashSet<>();
        n.walk(MethodCallExpr.class, mce -> {
            try {
                calledMethods.add(mce.resolve().getQualifiedSignature());
            } catch (Exception e) {
                // Unsolved symbol
            }
        });
        Metric metric = Metric.of(MetricType.RFC, Value.of(nom + calledMethods.size()));
        collector.accept(metric);
    }
}
