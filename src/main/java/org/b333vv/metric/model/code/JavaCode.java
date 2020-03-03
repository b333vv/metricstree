package org.b333vv.metric.model.code;

import com.google.common.base.Objects;
import com.intellij.psi.PsiElementVisitor;
import org.b333vv.metric.model.metric.Metric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class JavaCode {
    private Map<String, Metric> metrics;
    protected Set<JavaCode> children;
    private String name;
    private JavaCode parent = null;

    public JavaCode(String name) {
        this.name = name;
        this.children = new HashSet<>();
//        this.metrics = new ConcurrentHashMap<>();
        this.metrics = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Stream<Metric> getMetrics() {
        return metrics.values().stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaCode javaCode = (JavaCode) o;
        return Objects.equal(getName(), javaCode.getName()) &&
                Objects.equal(getParent(), javaCode.getParent());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getParent());
    }

    public void addMetric(Metric metric) {
        metrics.put(metric.getName(), metric);
    }

    public void addMetrics(Set<Metric> metrics) {
        for (Metric metric : metrics) {
            this.addMetric(metric);
        }
    }

    protected Set<JavaCode> getChildren() {
        return children;
    }

    protected JavaCode getParent() {
        return parent;
    }

    protected void addChild(JavaCode child) {
        child.parent = this;
        this.children.add(child);
    }

    protected void accept(PsiElementVisitor visitor) {

    }
}

