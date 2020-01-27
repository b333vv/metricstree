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
        metrics.put("NOC", new MetricStub("NOC", "Number of Children", false, 0.00, 0.00, 0, 10));
        metrics.put("DIT", new MetricStub("DIT", "Depth of Inheritance Tree", false, 0.00, 0.00, 0, 5));
        metrics.put("WMC", new MetricStub("WMC", "Weighted methods per Class", true, 0.00, 3.14, 0, 0));
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
