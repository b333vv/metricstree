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

                        // Align with PSI: include static fields as well
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

                    // Align with PSI: do not exclude calls chained from static methods
                    if (!declaringClassName.equals(currentClassName) && isSimpleAccessor(resolvedMethod)) {
                        foreignClasses.add(declaringClassName);
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
            // Debug: print foreign classes for CalculationServiceImpl
            if ("org.b333vv.metric.service.CalculationServiceImpl".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CalculationServiceImpl foreign classes before superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            // Remove current class and all its superclasses from the set
            com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration current = n.resolve();
            foreignClasses.remove(current.getQualifiedName());
            for (com.github.javaparser.resolution.types.ResolvedReferenceType superType : current.getAllAncestors()) {
                try {
                    foreignClasses.remove(superType.getQualifiedName());
                } catch (Exception e) {
                    // ignore
                }
            }
            if ("org.b333vv.metric.service.CalculationServiceImpl".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CalculationServiceImpl foreign classes after superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
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
        boolean isStatic = method.isStatic();
        if (isStatic) return false;
        // Try to get the method declaration node if available
        if (method instanceof com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration) {
            com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration jpMethod = (com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration) method;
            com.github.javaparser.ast.body.MethodDeclaration decl = jpMethod.getWrappedNode();
            // Simple getter: no params, non-void, body is 'return this.field;' or 'return field;'
            if (name.startsWith("get") && params == 0 && !method.getReturnType().isVoid()) {
                if (decl.getBody().isPresent()) {
                    com.github.javaparser.ast.stmt.BlockStmt body = decl.getBody().get();
                    if (body.getStatements().size() == 1 && body.getStatement(0).isReturnStmt()) {
                        com.github.javaparser.ast.stmt.ReturnStmt ret = body.getStatement(0).asReturnStmt();
                        if (ret.getExpression().isPresent() && ret.getExpression().get().isFieldAccessExpr()) {
                            return true;
                        }
                        if (ret.getExpression().isPresent() && ret.getExpression().get().isNameExpr()) {
                            // Could be 'return field;' (unqualified)
                            return true;
                        }
                    }
                }
            }
            // Simple boolean getter: 'is' prefix, no params, returns boolean, body is 'return this.field;' or 'return field;'
            if (name.startsWith("is") && params == 0 && method.getReturnType().isPrimitive() && method.getReturnType().describe().equals("boolean")) {
                if (decl.getBody().isPresent()) {
                    com.github.javaparser.ast.stmt.BlockStmt body = decl.getBody().get();
                    if (body.getStatements().size() == 1 && body.getStatement(0).isReturnStmt()) {
                        com.github.javaparser.ast.stmt.ReturnStmt ret = body.getStatement(0).asReturnStmt();
                        if (ret.getExpression().isPresent() && ret.getExpression().get().isFieldAccessExpr()) {
                            return true;
                        }
                        if (ret.getExpression().isPresent() && ret.getExpression().get().isNameExpr()) {
                            return true;
                        }
                    }
                }
            }
            // Simple setter: 'set' prefix, one param, void, body is 'this.field = param;' or 'field = param;'
            if (name.startsWith("set") && params == 1 && method.getReturnType().isVoid()) {
                if (decl.getBody().isPresent()) {
                    com.github.javaparser.ast.stmt.BlockStmt body = decl.getBody().get();
                    if (body.getStatements().size() == 1 && body.getStatement(0).isExpressionStmt()) {
                        com.github.javaparser.ast.expr.Expression expr = body.getStatement(0).asExpressionStmt().getExpression();
                        if (expr.isAssignExpr()) {
                            com.github.javaparser.ast.expr.AssignExpr assign = expr.asAssignExpr();
                            if (assign.getTarget().isFieldAccessExpr() || assign.getTarget().isNameExpr()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        // Reintroduce a conservative signature-based heuristic to approximate PSI's PropertyUtil
        // Simple getter: starts with "get", no params, non-void return
        boolean isGetter = name.startsWith("get") && params == 0 && !method.getReturnType().isVoid();
        // Simple boolean getter: starts with "is", no params, returns boolean
        boolean isBooleanGetter = name.startsWith("is") && params == 0 &&
                                  method.getReturnType().isPrimitive() &&
                                  method.getReturnType().describe().equals("boolean");
        // Simple setter: starts with "set", exactly one param, void return
        boolean isSetter = name.startsWith("set") && params == 1 && method.getReturnType().isVoid();
        return isGetter || isBooleanGetter || isSetter;
    }
}
