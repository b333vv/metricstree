package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JavaParserTightClassCohesionVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        // Align method selection and field usage detection with PSI implementation
        // 1) Select applicable methods: non-static, non-abstract, exclude boilerplate
        List<MethodDeclaration> methods = n.getMethods().stream()
                .filter(m -> !m.isStatic())
                .filter(m -> !m.isAbstract())
                .filter(m -> !BOILERPLATE_METHODS.contains(m.getNameAsString()))
                .collect(Collectors.toList());

        if (methods.size() < 2) {
            collector.accept(Metric.of(MetricType.TCC, Value.of(0.0)));
            return;
        }

        // 2) Collect instance fields declared in the same class (non-static)
        Set<String> instanceFieldNames = n.getFields().stream()
                .filter(fd -> !fd.isStatic())
                .flatMap((FieldDeclaration fd) -> fd.getVariables().stream())
                .map(VariableDeclarator::getNameAsString)
                .collect(Collectors.toSet());

        Map<MethodDeclaration, Set<String>> methodFieldUsage = new HashMap<>();
        for (MethodDeclaration method : methods) {
            Set<String> usedFields = new HashSet<>();

            // Unqualified instance field access: just a NameExpr matching declared instance fields
            method.walk(NameExpr.class, ne -> {
                String name = ne.getNameAsString();
                if (instanceFieldNames.contains(name)) {
                    usedFields.add(name);
                }
            });

            // Qualified with this: this.field
            method.walk(FieldAccessExpr.class, fae -> {
                if (fae.getScope() instanceof ThisExpr) {
                    String name = fae.getNameAsString();
                    if (instanceFieldNames.contains(name)) {
                        usedFields.add(name);
                    }
                }
            });

            methodFieldUsage.put(method, usedFields);
        }

        // 3) Count connected method pairs sharing at least one instance field
        int np = 0;
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<String> fields1 = methodFieldUsage.get(methods.get(i));
                Set<String> fields2 = methodFieldUsage.get(methods.get(j));
                if (!Collections.disjoint(fields1, fields2)) {
                    np++;
                }
            }
        }

        int numMethods = methods.size();
        double totalPairs = (double) numMethods * (numMethods - 1) / 2.0;
        double tcc = totalPairs > 0 ? (double) np / totalPairs : 0.0;

        collector.accept(Metric.of(MetricType.TCC, Value.of(tcc)));
    }

    private static final Set<String> BOILERPLATE_METHODS = Set.of(
            "toString", "equals", "hashCode", "finalize", "clone", "readObject", "writeObject"
    );
}
