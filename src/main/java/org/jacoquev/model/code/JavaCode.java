package org.jacoquev.model.code;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import org.jacoquev.model.metric.Metric;
import org.jacoquev.model.visitor.type.JavaClassVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public abstract class JavaCode {
    private final Map<String, Metric> metrics;
    private final Map<String, String> attributes;
    protected Set<JavaCode> children;
    private String name;
    private JavaCode parent = null;

    public JavaCode(String name) {
        this.name = name;
        this.children = new HashSet<>();
        this.metrics = new HashMap<>();
        this.attributes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Stream<Metric> getMetrics() {
//        return ImmutableSet.copyOf(metrics.values());
        return metrics.values().stream();
    }

    public Optional<Metric> getMetric(String name) {
        return Optional.ofNullable(this.metrics.get(name));
    }

    public Map<String, String> getAttributes() {
        return ImmutableMap.copyOf(attributes);
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
        metrics.put(metric.getName(), metric);
    }

    public synchronized void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public void addAttribute(Map.Entry<String, String> attribute) {
        addAttribute(attribute.getKey(), attribute.getValue());
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

