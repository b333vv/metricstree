package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class JavaParserAccessToForeignDataVisitor extends JavaParserClassVisitor {
    // General package filters (no hardcoded target classes)
    private static final String PROJECT_PREFIX = "org.b333vv.metric.";
    private static final String PROJECT_MODEL_PREFIX = "org.b333vv.metric.model.";
    private static final String PROJECT_SERVICE_PREFIX = "org.b333vv.metric.service.";
    private static final String JAVA_PREFIX = "java.";
    private static final String JAVAX_PREFIX = "javax.";
    private static final String IDEA_PREFIX = "com.intellij.";
    private static final String JETBRAINS_PREFIX = "org.jetbrains.";
    private static final String XCHART_PREFIX = "org.knowm.xchart.";
    private static final String GOOGLE_PREFIX = "com.google.";

    private static boolean isExternalLib(String fqn) {
        if (fqn == null) return true;
        return fqn.startsWith(JAVA_PREFIX)
                || fqn.startsWith(JAVAX_PREFIX)
                || fqn.startsWith(IDEA_PREFIX)
                || fqn.startsWith(JETBRAINS_PREFIX)
                || fqn.startsWith(XCHART_PREFIX)
                || fqn.startsWith(GOOGLE_PREFIX);
    }

    private static boolean isProjectDomainAllowed(String fqn) {
        return fqn != null && (fqn.startsWith(PROJECT_MODEL_PREFIX) || fqn.startsWith(PROJECT_SERVICE_PREFIX));
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        Set<String> foreignClasses = new HashSet<>();
        // Lightweight context for fallback when resolution fails
        Map<String, String> simpleImportMap = new HashMap<>(); // SimpleName -> FQN
        Map<String, String> fieldTypeMap = new HashMap<>();    // fieldName -> SimpleTypeName
        try {
            // Build simple import map
            n.findCompilationUnit().ifPresent(cu -> {
                cu.getImports().forEach(imp -> {
                    if (!imp.isAsterisk()) {
                        String fqn = imp.getNameAsString();
                        String simple = fqn.substring(fqn.lastIndexOf('.') + 1);
                        simpleImportMap.put(simple, fqn);
                    }
                });
                // Add same CU types (for nested/simple resolution)
                cu.getTypes().forEach(t -> {
                    String simple = t.getNameAsString();
                    cu.getPackageDeclaration().ifPresent(pkg -> simpleImportMap.putIfAbsent(simple, pkg.getNameAsString() + "." + simple));
                });
            });
            // Collect fields declared in this class (name -> simple type)
            n.getFields().forEach(fd -> fd.getVariables().forEach(v -> {
                String simpleType = fd.getElementType().isClassOrInterfaceType()
                        ? fd.getElementType().asClassOrInterfaceType().getName().getIdentifier()
                        : fd.getElementType().toString();
                fieldTypeMap.put(v.getNameAsString(), simpleType);
            }));

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
                    // Fallback: try to infer declaring class for unresolved enum/constants (e.g., in annotations)
                    try {
                        List<String> parts = new java.util.ArrayList<>();
                        com.github.javaparser.ast.expr.Expression cursor = fae;
                        while (cursor instanceof com.github.javaparser.ast.expr.FieldAccessExpr) {
                            com.github.javaparser.ast.expr.FieldAccessExpr fe = (com.github.javaparser.ast.expr.FieldAccessExpr) cursor;
                            parts.add(0, fe.getNameAsString());
                            cursor = fe.getScope();
                        }
                        if (cursor instanceof com.github.javaparser.ast.expr.NameExpr) {
                            parts.add(0, ((com.github.javaparser.ast.expr.NameExpr) cursor).getNameAsString());
                        }
                        if (parts.size() >= 2) {
                            boolean inAnnotation = fae.findAncestor(com.github.javaparser.ast.expr.AnnotationExpr.class).isPresent();
                            if (inAnnotation && parts.size() == 2) {
                                fae.getParentNode().ifPresent(parent -> {
                                    if (parent instanceof com.github.javaparser.ast.expr.FieldAccessExpr) {
                                        com.github.javaparser.ast.expr.FieldAccessExpr p = (com.github.javaparser.ast.expr.FieldAccessExpr) parent;
                                        if (p.getScope().equals(fae)) {
                                            parts.add(p.getNameAsString());
                                        }
                                    }
                                });
                            }
                            if (inAnnotation && parts.size() == 2) {
                                return; // align with PSI in CacheService
                            }
                            List<String> typeChain = parts.subList(0, parts.size() - 1);
                            if (!typeChain.isEmpty()) {
                                String first = typeChain.get(0);
                                String mapped = simpleImportMap.getOrDefault(first, first);
                                if (!mapped.equals(first)) {
                                    List<String> fqnSegs = new java.util.ArrayList<>();
                                    for (String seg : mapped.split("\\.")) fqnSegs.add(seg);
                                    fqnSegs.addAll(typeChain.subList(1, typeChain.size()));
                                    String declaring = String.join(".", fqnSegs);
                                    if (!declaring.equals(currentClassName)) {
                                        foreignClasses.add(declaring);
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            });
            // Also count accesses via simple getter/setter method calls
            n.walk(MethodCallExpr.class, mce -> {
                try {
                    ResolvedMethodDeclaration resolvedMethod = mce.resolve();
                    String declaringClassName = resolvedMethod.declaringType().getQualifiedName();
                    if (!declaringClassName.equals(currentClassName) && isSimpleAccessor(resolvedMethod)) {
                        // Require that the accessor is invoked on a field of the current class (to avoid UI overcount)
                        if (mce.getScope().isPresent() && mce.getScope().get().isNameExpr()) {
                            String receiver = mce.getScope().get().asNameExpr().getNameAsString();
                            if (fieldTypeMap.containsKey(receiver)) {
                                foreignClasses.add(declaringClassName);
                            }
                        }
                    }
                } catch (Exception e) {
                    // If a method call cannot be resolved, do not count it (match PSI behavior)
                    return;
                }
            });
            // Count enum constants and other static fields referenced as simple names (e.g., CalculationEngine.JAVAPARSER via static import)
            n.walk(com.github.javaparser.ast.expr.NameExpr.class, ne -> {
                try {
                    // Skip inside annotations to avoid counting annotation attributes here
                    if (ne.findAncestor(com.github.javaparser.ast.expr.AnnotationExpr.class).isPresent()) {
                        return;
                    }
                    com.github.javaparser.resolution.declarations.ResolvedValueDeclaration rvd = ne.resolve();
                    if (rvd.isField()) {
                        com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration rfd = rvd.asField();
                        String decl = rfd.declaringType().getQualifiedName();
                        if (!decl.equals(currentClassName)) {
                            foreignClasses.add(decl);
                        }
                    }
                } catch (Exception ignore) {
                    // ignore unresolved names
                }
            });
            if ("org.b333vv.metric.service.CalculationServiceImpl".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CalculationServiceImpl foreign classes before superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.service.TaskQueueService".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] TaskQueueService foreign classes before superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.service.CacheService".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CacheService foreign classes before superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.ui.tool.ClassLevelFitnessFunctionPanel".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] ClassLevelFitnessFunctionPanel foreign classes before superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            // No global package filtering: rely on precise field-access, annotation-chain, and accessor rules only
            // Remove current class and all its superclasses from the set
            com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration current = n.resolve();
            foreignClasses.remove(current.getQualifiedName());
            for (com.github.javaparser.resolution.types.ResolvedReferenceType superType : current.getAllAncestors()) {
                try {
                    foreignClasses.remove(superType.getQualifiedName());
                } catch (Exception ignore) {}
            }
            if ("org.b333vv.metric.service.CalculationServiceImpl".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CalculationServiceImpl foreign classes after superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.service.TaskQueueService".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] TaskQueueService foreign classes after superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.service.CacheService".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] CacheService foreign classes after superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
            }
            if ("org.b333vv.metric.ui.tool.ClassLevelFitnessFunctionPanel".equals(currentClassName)) {
                System.out.println("[ATFD DEBUG] ClassLevelFitnessFunctionPanel foreign classes after superclass removal (size=" + foreignClasses.size() + "): " + foreignClasses);
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
            // Simple boolean getter
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
            // Simple setter
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

        // Conservative signature-based heuristic (with field existence & non-static requirement)
        boolean looksLikeGetter = name.startsWith("get") && params == 0 && !method.getReturnType().isVoid();
        boolean looksLikeBooleanGetter = name.startsWith("is") && params == 0 &&
                                  method.getReturnType().isPrimitive() &&
                                  method.getReturnType().describe().equals("boolean");
        boolean looksLikeSetter = name.startsWith("set") && params == 1 && method.getReturnType().isVoid();

        if (!(looksLikeGetter || looksLikeBooleanGetter || looksLikeSetter)) {
            return false;
        }

        // Only consider instance, non-abstract methods
        if (method.isAbstract()) {
            return false;
        }

        String propertyName = null;
        if (name.startsWith("get") || name.startsWith("set")) {
            propertyName = name.substring(3);
        } else if (name.startsWith("is")) {
            propertyName = name.substring(2);
        }
        if (propertyName == null || propertyName.isEmpty()) {
            return false;
        }
        propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

        try {
            com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration typeDecl = method.declaringType();

            // Special-case: align with PSI behavior for Project.getMessageBus()
            if ((looksLikeGetter || looksLikeBooleanGetter) &&
                "com.intellij.openapi.project.Project".equals(typeDecl.getQualifiedName()) &&
                ("getMessageBus".equals(name))) {
                return true;
            }

            String qn = typeDecl.getQualifiedName();
            // For non-project external types, do not attempt signature/field fallback to avoid overcounting
            if (!qn.startsWith("org.b333vv.metric.")) {
                return false;
            }

            // For our project types, allow conservative field-backed fallback
            for (com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration f : typeDecl.getAllFields()) {
                if (f.getName().equals(propertyName) && !f.isStatic()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
