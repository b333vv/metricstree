package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
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
        if (n.isInterface()) {
            collector.accept(Metric.of(MetricType.RFC, Value.UNDEFINED));
            return;
        }
        
        Set<String> uniqueTargets = new HashSet<>();
        String className = n.getFullyQualifiedName().orElse(n.getNameAsString());

        n.getMethods().forEach(method -> {
            try {
                uniqueTargets.add(normalizeSignature(method.resolve().getQualifiedSignature()));
            } catch (Exception e) {
                uniqueTargets.add(normalizeSignature(className + "#" + method.getSignature().asString()));
            }
        });

        n.getConstructors().forEach(constructor -> {
            try {
                uniqueTargets.add(normalizeSignature(constructor.resolve().getQualifiedSignature()));
            } catch (Exception e) {
                uniqueTargets.add(normalizeSignature(className + "#" + constructor.getSignature().asString()));
            }
        });

        RFCInvocationCollector collectorVisitor = new RFCInvocationCollector();

        n.getMembers().forEach(member -> {
            if (member instanceof ClassOrInterfaceDeclaration
                    || member instanceof EnumDeclaration
                    || member instanceof AnnotationDeclaration
                    || member instanceof RecordDeclaration) {
                return;
            }
            if (member instanceof MethodDeclaration methodDeclaration) {
                methodDeclaration.getBody().ifPresent(body -> body.accept(collectorVisitor, uniqueTargets));
            } else if (member instanceof ConstructorDeclaration constructorDeclaration) {
                constructorDeclaration.getBody().accept(collectorVisitor, uniqueTargets);
            } else if (member instanceof FieldDeclaration fieldDeclaration) {
                fieldDeclaration.getVariables().forEach(variable ->
                        variable.getInitializer().ifPresent(expression -> expression.accept(collectorVisitor, uniqueTargets)));
            } else if (member instanceof InitializerDeclaration initializerDeclaration) {
                initializerDeclaration.getBody().accept(collectorVisitor, uniqueTargets);
            } else if (member instanceof EnumConstantDeclaration enumConstantDeclaration) {
                enumConstantDeclaration.getArguments().forEach(argument -> argument.accept(collectorVisitor, uniqueTargets));
            }
        });

        Metric metric = Metric.of(MetricType.RFC, Value.of(uniqueTargets.size()));
        collector.accept(metric);
    }

    private static class RFCInvocationCollector extends VoidVisitorAdapter<Set<String>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Set<String> collector) {
            // Do not traverse nested or local classes.
        }

        @Override
        public void visit(EnumDeclaration n, Set<String> collector) {
            // Skip nested enum declarations.
        }

        @Override
        public void visit(AnnotationDeclaration n, Set<String> collector) {
            // Skip nested annotation declarations.
        }

        @Override
        public void visit(RecordDeclaration n, Set<String> collector) {
            // Skip nested record declarations.
        }

        @Override
        public void visit(SuperExpr n, Set<String> collector) {
            // Avoid descending into implicit super expressions to prevent duplicate traversal.
        }

        @Override
        public void visit(MethodCallExpr n, Set<String> collector) {
            super.visit(n, collector);
            try {
                collector.add(normalizeSignature(n.resolve().getQualifiedSignature()));
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        }

        @Override
        public void visit(ObjectCreationExpr n, Set<String> collector) {
            n.getScope().ifPresent(scope -> scope.accept(this, collector));
            n.getArguments().forEach(argument -> argument.accept(this, collector));
            try {
                collector.add(normalizeSignature(n.resolve().getQualifiedSignature()));
            } catch (Exception ignored) {
                // Unsolved symbol
            }
            // Skip anonymous class body to avoid counting nested class calls.
        }

        @Override
        public void visit(MethodReferenceExpr n, Set<String> collector) {
            super.visit(n, collector);
            try {
                collector.add(normalizeSignature(n.resolve().getQualifiedSignature()));
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        }

        @Override
        public void visit(ExplicitConstructorInvocationStmt n, Set<String> collector) {
            n.getArguments().forEach(argument -> argument.accept(this, collector));
            try {
                collector.add(normalizeSignature(n.resolve().getQualifiedSignature()));
            } catch (Exception ignored) {
                // Unsolved symbol
            }
        }
    }

    private static String normalizeSignature(String signature) {
        if (signature == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder(signature.length());
        int depth = 0;
        for (int i = 0; i < signature.length(); i++) {
            char ch = signature.charAt(i);
            if (ch == '<') {
                depth++;
                continue;
            }
            if (ch == '>') {
                if (depth > 0) {
                    depth--;
                }
                continue;
            }
            if (depth == 0 && ch != ' ') {
                builder.append(ch);
            }
        }
        return builder.toString();
    }
}
