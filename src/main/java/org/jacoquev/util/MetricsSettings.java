package org.jacoquev.util;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@State(name = "MetricsSettings", storages = {@Storage("metrics.xml")})
public final class MetricsSettings implements PersistentStateComponent<MetricsSettings>, ProjectComponent {

    private Map<String, MetricStub> metrics = new HashMap<>();

    public MetricsSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        metrics.put("WMC", new MetricStub("WMC", "Weighted Methods Per Class",
                false, 0.00, 0.00, 0, 100));
        metrics.put("DIT", new MetricStub("DIT", "Depth Of Inheritance Tree",
                false, 0.00, 0.00, 0, 5));
        metrics.put("NOC", new MetricStub("NOC", "Number Of Children",
                false, 0.00, 0.00, 0, 100));
        metrics.put("CBO", new MetricStub("CBO", "Coupling Between Object",
                false, 0.00, 0.00, 0, 5));
        metrics.put("RFC", new MetricStub("RFC", "Response For A Class",
                false, 0.00, 0.00, 0, 100));
        metrics.put("LCOM", new MetricStub("LCOM", "Lack Of Cohesion In Methods",
                false, 0.00, 0.00, 0, 500));

        //Lorenz-Kidd metrics set
        metrics.put("NOA", new MetricStub("NOA", "Number Of Attributes",
                false, 0.00, 0.00, 0, 30));
        metrics.put("NOO", new MetricStub("NOO", "Number Of Operations",
                false, 0.00, 0.00, 0, 100));
        metrics.put("NOAM", new MetricStub("NOAM", "Number Of Added Methods",
                false, 0.00, 0.00, 0, 10));
        metrics.put("NOOM", new MetricStub("NOOM", "Number of Overridden Methods",
                false, 0.00, 0.00, 0, 5));

        metrics.put("SIZE2", new MetricStub("SIZE2", "Number Of Attributes And Methods",
                false, 0.00, 0.00, 0, 130));

        //Robert C. Martin metrics set
        metrics.put("Ce", new MetricStub("Ce", "Efferent Coupling",
                false, 0.00, 0.00, 0, 10));
        metrics.put("Ca", new MetricStub("Ca", "Afferent Coupling",
                false, 0.00, 0.00, 0, 10));
        metrics.put("I", new MetricStub("I", "Instability",
                true, 0.00, 1.00, 0, 0));
        metrics.put("A", new MetricStub("A", "Abstractness",
                true, 0.00, 1.00, 0, 0));
        metrics.put("D", new MetricStub("D", "Normalized Distance From Main Sequence",
                true, 0.00, 1.00, 0, 0));

        //MOOD metrics set
        metrics.put("MHF", new MetricStub("MHF", "Method Hiding Factor",
                true, 0.095, 0.369, 0, 0));
        metrics.put("AHF", new MetricStub("AHF", "Attribute Hiding Factor",
                true, 0.677, 1.0, 0, 0));
        metrics.put("MIF", new MetricStub("MIF", "Method Inheritance Factor",
                true, 0.609, 0.844, 0, 0));
        metrics.put("AIF", new MetricStub("AIF", "Attribute Inheritance Factor",
                true, 0.374, 0.757, 0, 0));
        metrics.put("CF", new MetricStub("CF", "Coupling Factor",
                true, 0.00, 0.243, 0, 0));
        metrics.put("PF", new MetricStub("PF", "Polymorphism Factor",
                true, 0.017, 0.151, 0, 0));

        //Methods metrics set
        metrics.put("CND", new MetricStub("CND", "Condition Nesting Depth",
                false, 0.00, 0.00, 0, 50));
        metrics.put("LOC", new MetricStub("LOC", "Lines Of Code",
                false, 0.00, 0.00, 0, 200));
        metrics.put("LND", new MetricStub("LND", "Loop Nesting Depth",
                false, 0.00, 0.00, 0, 4));
        metrics.put("CC", new MetricStub("CC", "McCabe Cyclomatic Complexity",
                false, 0.00, 0.00, 0, 50));
        metrics.put("NOL", new MetricStub("NOL", "Number Of Loops",
                false, 0.00, 0.00, 0, 10));
    }

    public Map<String, MetricStub> getMetrics() {
        return new HashMap<>(metrics);
    }

    public void setMetrics(Map<String, MetricStub> metrics) {
        this.metrics.clear();
        this.metrics.putAll(metrics);
    }

    public List<MetricStub> getMetricsList() {
        return new ArrayList<>(metrics.values());
    }

    public void updateMetrics(@NotNull HashMap<String, MetricStub> newMetrics) {
        newMetrics.forEach((key, value) -> metrics.replace(key, value));
    }

    @Override
    public synchronized MetricsSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(MetricsSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MetricsSettings";
    }

    public static class MetricStub {
        private String name;
        private String description;
        private boolean doubleValue;
        private double minDoubleValue;
        private double maxDoubleValue;
        private long minLongValue;
        private long maxLongValue;

        public MetricStub(String name, String description, boolean doubleValue,
                          double minDoubleValue, double maxDoubleValue, long minLongValue, long maxLongValue) {
            this.name = name;
            this.description = description;
            this.doubleValue = doubleValue;
            this.minDoubleValue = minDoubleValue;
            this.maxDoubleValue = maxDoubleValue;
            this.minLongValue = minLongValue;
            this.maxLongValue = maxLongValue;
        }

        public MetricStub() {
        }

        public boolean isDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(boolean doubleValue) {
            this.doubleValue = doubleValue;
        }

        public double getMinDoubleValue() {
            return minDoubleValue;
        }

        public void setMinDoubleValue(double minDoubleValue) {
            this.minDoubleValue = minDoubleValue;
        }

        public double getMaxDoubleValue() {
            return maxDoubleValue;
        }

        public void setMaxDoubleValue(double maxDoubleValue) {
            this.maxDoubleValue = maxDoubleValue;
        }

        public long getMinLongValue() {
            return minLongValue;
        }

        public void setMinLongValue(long minLongValue) {
            this.minLongValue = minLongValue;
        }

        public long getMaxLongValue() {
            return maxLongValue;
        }

        public void setMaxLongValue(long maxLongValue) {
            this.maxLongValue = maxLongValue;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MetricStub)) return false;
            MetricStub that = (MetricStub) o;
            return doubleValue == that.doubleValue &&
                    Double.compare(that.getMinDoubleValue(), getMinDoubleValue()) == 0 &&
                    Double.compare(that.getMaxDoubleValue(), getMaxDoubleValue()) == 0 &&
                    getMinLongValue() == that.getMinLongValue() &&
                    getMaxLongValue() == that.getMaxLongValue() &&
                    getName().equals(that.getName()) &&
                    Objects.equals(getDescription(), that.getDescription());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getDescription(), doubleValue, getMinDoubleValue(), getMaxDoubleValue(), getMinLongValue(), getMaxLongValue());
        }
    }

}
