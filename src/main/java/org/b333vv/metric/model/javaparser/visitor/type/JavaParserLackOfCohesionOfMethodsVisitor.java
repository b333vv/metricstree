package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
import org.b333vv.metric.model.visitor.type.CohesionUtils;
import org.b333vv.metric.model.metric.Metric;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;

import java.util.*;
import java.util.function.Consumer;

public class JavaParserLackOfCohesionOfMethodsVisitor extends JavaParserClassVisitor {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Consumer<Metric> collector) {
        List<MethodDeclaration> methods = n.getMethods();
        List<FieldDeclaration> fields = n.getFields();

        if (methods.isEmpty() || fields.isEmpty()) {
            collector.accept(Metric.of(MetricType.LCOM, Value.of(0)));
            return;
        }

        // Filter out static and boilerplate methods - only consider applicable instance methods
        List<MethodDeclaration> instanceMethods = new ArrayList<>();
        Set<String> boilerplate = CohesionUtils.getBoilerplateMethods();
        for (MethodDeclaration method : methods) {
            if (!method.isStatic() && !boilerplate.contains(method.getNameAsString())) {
                instanceMethods.add(method);
            }
        }

        // If no instance methods, LCOM is 0
        if (instanceMethods.isEmpty()) {
            collector.accept(Metric.of(MetricType.LCOM, Value.of(0)));
            return;
        }

        Map<MethodDeclaration, Set<String>> methodFieldUsage = new HashMap<>();
        for (MethodDeclaration method : instanceMethods) {
            Set<String> usedFields = new HashSet<>();
            method.walk(FieldAccessExpr.class, fae -> {
                try {
                    if (fae.resolve().isField() && n.resolve().getQualifiedName().equals(fae.resolve().asField().declaringType().getQualifiedName())) {
                        // Only include non-static instance fields
                        if (!fae.resolve().asField().isStatic()) {
                            usedFields.add(fae.getNameAsString());
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
            // Also consider unqualified field references (e.g., fieldA = 1;)
            method.walk(NameExpr.class, ne -> {
                try {
                    var resolved = ne.resolve();
                    if (resolved.isField()) {
                        var field = resolved.asField();
                        if (!field.isStatic() && n.resolve().getQualifiedName().equals(field.declaringType().getQualifiedName())) {
                            usedFields.add(ne.getNameAsString());
                        }
                    }
                } catch (Exception e) {
                    // ignore resolution issues
                }
            });
            methodFieldUsage.put(method, usedFields);
        }

        // Filter out methods that don't use any fields
        List<MethodDeclaration> methodsUsingFields = new ArrayList<>();
        for (Map.Entry<MethodDeclaration, Set<String>> entry : methodFieldUsage.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                methodsUsingFields.add(entry.getKey());
            }
        }
        
        // If no methods use fields, LCOM is 0
        if (methodsUsingFields.isEmpty()) {
            collector.accept(Metric.of(MetricType.LCOM, Value.of(0)));
            return;
        }

        Graph graph = new Graph(methodsUsingFields.size());
        for (int i = 0; i < methodsUsingFields.size(); i++) {
            for (int j = i + 1; j < methodsUsingFields.size(); j++) {
                MethodDeclaration m1 = methodsUsingFields.get(i);
                MethodDeclaration m2 = methodsUsingFields.get(j);
                Set<String> fields1 = methodFieldUsage.get(m1);
                Set<String> fields2 = methodFieldUsage.get(m2);
                if (!Collections.disjoint(fields1, fields2)) {
                    graph.addEdge(i, j);
                }
            }
        }

        // Add method-call linkage between applicable methods (same-class calls)
        // Build a quick index from method to its list index for O(1) lookup
        Map<MethodDeclaration, Integer> indexByMethod = new IdentityHashMap<>();
        for (int idx = 0; idx < methodsUsingFields.size(); idx++) {
            indexByMethod.put(methodsUsingFields.get(idx), idx);
        }

        // Also index candidate callees by (name, arity) for heuristic matching when resolution fails
        Map<String, List<MethodDeclaration>> methodsByName = new HashMap<>();
        for (MethodDeclaration md : methodsUsingFields) {
            methodsByName.computeIfAbsent(md.getNameAsString(), k -> new ArrayList<>()).add(md);
        }

        String classQName = null;
        try {
            classQName = n.resolve().getQualifiedName();
        } catch (Exception ignored) {
        }

        for (int i = 0; i < methodsUsingFields.size(); i++) {
            final int callerIdx = i;
            MethodDeclaration caller = methodsUsingFields.get(i);
            final String finalClassQName = classQName; // effectively final for lambda
            caller.walk(MethodCallExpr.class, mce -> {
                // Try resolve to ensure it's an intra-class call
                try {
                    var resolved = mce.resolve();
                    var declType = resolved.declaringType();
                    if (finalClassQName != null && finalClassQName.equals(declType.getQualifiedName())) {
                        String name = resolved.getName();
                        int arity = resolved.getNumberOfParams();
                        List<MethodDeclaration> cands = methodsByName.getOrDefault(name, Collections.emptyList());
                        for (MethodDeclaration target : cands) {
                            if (target.getParameters().size() == arity) {
                                Integer j = indexByMethod.get(target);
                                if (j != null && j != callerIdx) {
                                    graph.addEdge(callerIdx, j);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Fall back: heuristic by unqualified name and argument count if resolution fails
                    String name = mce.getNameAsString();
                    int arity = mce.getArguments().size();
                    List<MethodDeclaration> cands = methodsByName.getOrDefault(name, Collections.emptyList());
                    for (MethodDeclaration target : cands) {
                        if (target.getParameters().size() == arity) {
                            Integer j = indexByMethod.get(target);
                            if (j != null && j != callerIdx) {
                                graph.addEdge(callerIdx, j);
                            }
                        }
                    }
                }
            });
        }

        collector.accept(Metric.of(MetricType.LCOM, Value.of(graph.connectedComponents())));
    }

    private static class Graph {
        private final int V;
        private final List<List<Integer>> adj;

        Graph(int V) {
            this.V = V;
            adj = new ArrayList<>(V);
            for (int i = 0; i < V; i++) {
                adj.add(new ArrayList<>());
            }
        }

        void addEdge(int v, int w) {
            adj.get(v).add(w);
            adj.get(w).add(v);
        }

        void DFSUtil(int v, boolean[] visited) {
            visited[v] = true;
            for (int x : adj.get(v)) {
                if (!visited[x]) {
                    DFSUtil(x, visited);
                }
            }
        }

        int connectedComponents() {
            int count = 0;
            boolean[] visited = new boolean[V];
            for (int v = 0; v < V; ++v) {
                if (!visited[v]) {
                    DFSUtil(v, visited);
                    count++;
                }
            }
            return count;
        }
    }
}
