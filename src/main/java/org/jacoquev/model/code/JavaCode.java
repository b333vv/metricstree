package org.jacoquev.model.code;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.type.JavaClassVisitor;
import org.jacoquev.util.MetricsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class JavaCode {
    private final Set<Metric> metrics;
    protected Set<JavaCode> children;
    private String name;
    private JavaCode parent = null;

    public JavaCode(String name) {
        this.name = name;
        this.children = new HashSet<>();
        this.metrics = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Stream<Metric> getMetrics() {
        return metrics.stream();
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

    public synchronized void addMetric(Metric metric) {
        metrics.add(metric);
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

