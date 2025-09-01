package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class JavaParserAccessToForeignDataVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> foreignClasses = new HashSet<>();
        try {
            String currentClassName = n.resolve().getQualifiedName();
            n.walk(FieldAccessExpr.class, fae -> {
                try {
                    if (fae.resolve().isField()) {
                        ResolvedFieldDeclaration resolvedField = fae.resolve().asField();
                        String declaringClassName = resolvedField.declaringType().getQualifiedName();
                        if (!declaringClassName.equals(currentClassName)) {
                            foreignClasses.add(declaringClassName);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
            // Add: also count accesses via simple getter/setter method calls
            n.walk(MethodCallExpr.class, mce -> {
                try {
                    ResolvedMethodDeclaration resolvedMethod = mce.resolve();
                    String declaringClassName = resolvedMethod.declaringType().getQualifiedName();
                    if (!declaringClassName.equals(currentClassName) && isSimpleAccessor(resolvedMethod)) {
                        foreignClasses.add(declaringClassName);
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
        } catch (Exception e) {
            // ignore
        }
        Metric metric = Metric.of(MetricType.ATFD, Value.of(foreignClasses.size()));
        collector.accept(metric);
    }

    // Add this helper method to detect simple getter/setter
    private boolean isSimpleAccessor(ResolvedMethodDeclaration method) {
        String name = method.getName();
        int params = method.getNumberOfParams();
        boolean isGetter = (name.startsWith("get") && params == 0 && !method.getReturnType().isVoid()) ||
                           (name.startsWith("is") && params == 0 && method.getReturnType().isPrimitive() && method.getReturnType().describe().equals("boolean"));
        boolean isSetter = name.startsWith("set") && params == 1 && method.getReturnType().isVoid();
        // Heuristic: only count as simple if method is not static
        boolean isSimple = !method.isStatic();
        return isSimple && (isGetter || isSetter);
    }
}
