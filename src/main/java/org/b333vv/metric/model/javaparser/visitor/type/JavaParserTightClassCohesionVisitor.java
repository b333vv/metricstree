package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
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
        List<MethodDeclaration> publicMethods = n.getMethods().stream()
                .filter(MethodDeclaration::isPublic)
                .collect(Collectors.toList());

        if (publicMethods.size() < 2) {
            collector.accept(Metric.of(MetricType.TCC, Value.of(1.0)));
            return;
        }

        try {
            String currentClassName = n.resolve().getQualifiedName();
            Map<MethodDeclaration, Set<String>> methodFieldUsage = new HashMap<>();
            for (MethodDeclaration method : publicMethods) {
                Set<String> usedFields = new HashSet<>();
                method.walk(FieldAccessExpr.class, fae -> {
                    try {
                        if (fae.resolve().isField() && fae.resolve().asField().declaringType().getQualifiedName().equals(currentClassName)) {
                            usedFields.add(fae.getNameAsString());
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                });
                methodFieldUsage.put(method, usedFields);
            }

            int np = 0;
            for (int i = 0; i < publicMethods.size(); i++) {
                for (int j = i + 1; j < publicMethods.size(); j++) {
                    MethodDeclaration m1 = publicMethods.get(i);
                    MethodDeclaration m2 = publicMethods.get(j);
                    Set<String> fields1 = methodFieldUsage.get(m1);
                    Set<String> fields2 = methodFieldUsage.get(m2);
                    if (!Collections.disjoint(fields1, fields2)) {
                        np++;
                    }
                }
            }

            int numMethods = publicMethods.size();
            double totalPairs = (double) numMethods * (numMethods - 1) / 2.0;
            double tcc = totalPairs > 0 ? (double) np / totalPairs : 1.0;

            collector.accept(Metric.of(MetricType.TCC, Value.of(tcc)));

        } catch (Exception e) {
            collector.accept(Metric.of(MetricType.TCC, Value.UNDEFINED));
        }
    }
}
