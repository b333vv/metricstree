package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class JavaParserAccessToForeignDataVisitor extends JavaParserClassVisitor {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        final Set<String> usedClasses = new HashSet<>();

        try {
            String currentClassName = n.resolve().getQualifiedName();

            // прямые обращения к полям
            n.walk(FieldAccessExpr.class, fae -> {
                try {
                    ResolvedValueDeclaration rvd = fae.resolve();
                    if (rvd.isField()) {
                        ResolvedFieldDeclaration rfd = rvd.asField();
                        if (!rfd.isStatic()) {
                            String owner = rfd.declaringType().getQualifiedName();
                            usedClasses.add(owner);
                        }
                    }
                } catch (Throwable ignore) {}
            });

            // обращения к полям через простое имя
            n.walk(NameExpr.class, ne -> {
                try {
                    ResolvedValueDeclaration rvd = ne.resolve();
                    if (rvd.isField()) {
                        ResolvedFieldDeclaration rfd = rvd.asField();
                        if (!rfd.isStatic()) {
                            String owner = rfd.declaringType().getQualifiedName();
                            usedClasses.add(owner);
                        }
                    }
                } catch (Throwable ignore) {}
            });

            // вызовы методов-аксессоров
            n.walk(MethodCallExpr.class, mce -> {
                try {
                    ResolvedMethodDeclaration method = mce.resolve();
                    if (!method.isStatic() && isAccessor(method)) {
                        String owner = method.declaringType().getQualifiedName();
                        usedClasses.add(owner);
                    }
                } catch (Throwable ignore) {}
            });

            // убираем сам класс и его родителей
            ResolvedReferenceTypeDeclaration current = n.resolve();
            usedClasses.remove(current.getQualifiedName());
            for (ResolvedReferenceType superType : current.getAllAncestors()) {
                try {
                    usedClasses.remove(superType.getQualifiedName());
                } catch (Throwable ignore) {}
            }
        } catch (Throwable ignore) {}

        collector.accept(Metric.of(MetricType.ATFD, Value.of(usedClasses.size())));
    }

    private boolean isAccessor(ResolvedMethodDeclaration method) {
        String name = method.getName();
        int params = method.getNumberOfParams();

        if (name.startsWith("get") && params == 0 && !method.getReturnType().isVoid()) {
            return true;
        }
        if (name.startsWith("is") && params == 0 &&
                method.getReturnType().isPrimitive() &&
                "boolean".equals(method.getReturnType().describe())) {
            return true;
        }
        if (name.startsWith("set") && params == 1 && method.getReturnType().isVoid()) {
            return true;
        }
        return false;
    }
}