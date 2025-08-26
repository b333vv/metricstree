package org.b333vv.metric.model.javaparser.visitor.type;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import org.b333vv.metric.model.javaparser.visitor.JavaParserClassVisitor;
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

        Map<MethodDeclaration, Set<String>> methodFieldUsage = new HashMap<>();
        for (MethodDeclaration method : methods) {
            Set<String> usedFields = new HashSet<>();
            method.walk(FieldAccessExpr.class, fae -> {
                try {
                    if (fae.resolve().isField() && n.resolve().getQualifiedName().equals(fae.resolve().asField().declaringType().getQualifiedName())) {
                        usedFields.add(fae.getNameAsString());
                    }
                } catch (Exception e) {
                    // ignore
                }
            });
            methodFieldUsage.put(method, usedFields);
        }

        Graph graph = new Graph(methods.size());
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                MethodDeclaration m1 = methods.get(i);
                MethodDeclaration m2 = methods.get(j);
                Set<String> fields1 = methodFieldUsage.get(m1);
                Set<String> fields2 = methodFieldUsage.get(m2);
                if (!Collections.disjoint(fields1, fields2)) {
                    graph.addEdge(i, j);
                }
            }
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
