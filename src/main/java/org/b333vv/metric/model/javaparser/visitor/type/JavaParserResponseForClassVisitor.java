package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
        Set<String> methods = new HashSet<>();
        String className = n.getFullyQualifiedName().orElse(n.getNameAsString());

        n.getMethods().forEach(method -> {
            try {
                methods.add(method.resolve().getQualifiedSignature());
            } catch (Exception e) {
                methods.add(className + "#" + method.getSignature().asString());
            }
        });

        n.findAll(MethodCallExpr.class, expr -> belongsToClass(expr, n)).forEach(mce -> {
            try {
                methods.add(mce.resolve().getQualifiedSignature());
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        });

        n.findAll(ObjectCreationExpr.class, expr -> belongsToClass(expr, n)).forEach(oce -> {
            try {
                methods.add(oce.resolve().getQualifiedSignature());
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        });

        n.findAll(MethodReferenceExpr.class, expr -> belongsToClass(expr, n)).forEach(mre -> {
            try {
                methods.add(mre.resolve().getQualifiedSignature());
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        });

        Metric metric = Metric.of(MetricType.RFC, Value.of(methods.size()));
        collector.accept(metric);
    }

    private boolean belongsToClass(com.github.javaparser.ast.Node node, ClassOrInterfaceDeclaration current) {
        return node.findAncestor(ClassOrInterfaceDeclaration.class, ancestor -> ancestor == current).isPresent();
    }
}
