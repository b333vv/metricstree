package org.jacoquev.ui.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@State(name = "MetricsAllowableValueRanges", storages = {@Storage("metrics-allowed-value-ranges.xml")})
public final class MetricsAllowableValuesRanges implements PersistentStateComponent<MetricsAllowableValuesRanges>, ProjectComponent {

    private Map<String, MetricsAllowableValueRangeStub> metrics = new HashMap<>();

    public MetricsAllowableValuesRanges() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        metrics.put("WMC", new MetricsAllowableValueRangeStub("WMC", "Weighted Methods Per Class",
                false, 0.00, 0.00, 0, 24));
        metrics.put("DIT", new MetricsAllowableValueRangeStub("DIT", "Depth Of Inheritance Tree",
                false, 0.00, 0.00, 0, 5));
//        metrics.put("NOC", new MetricStub("NOC", "Number Of Children",
//                false, 0.00, 0.00, 0, 100));
        metrics.put("CBO", new MetricsAllowableValueRangeStub("CBO", "Coupling Between Object",
                false, 0.00, 0.00, 0, 13));
        metrics.put("RFC", new MetricsAllowableValueRangeStub("RFC", "Response For A Class",
                false, 0.00, 0.00, 0, 44));
//        metrics.put("LCOM", new MetricStub("LCOM", "Lack Of Cohesion In Methods",
//                false, 0.00, 0.00, 0, 500));

        //Lorenz-Kidd metrics set
        metrics.put("NOA", new MetricsAllowableValueRangeStub("NOA", "Number Of Attributes",
                false, 0.00, 0.00, 0, 40));
        metrics.put("NOO", new MetricsAllowableValueRangeStub("NOO", "Number Of Operations",
                false, 0.00, 0.00, 0, 20));
//        metrics.put("NOAM", new MetricStub("NOAM", "Number Of Added Methods",
//                false, 0.00, 0.00, 0, 10));
        metrics.put("NOOM", new MetricsAllowableValueRangeStub("NOOM", "Number of Overridden Methods",
                false, 0.00, 0.00, 0, 3));

//        metrics.put("SIZE2", new MetricStub("SIZE2", "Number Of Attributes And Methods",
//                false, 0.00, 0.00, 0, 130));

        //Robert C. Martin metrics set
        metrics.put("Ce", new MetricsAllowableValueRangeStub("Ce", "Efferent Coupling",
                false, 0.00, 0.00, 0, 20));
        metrics.put("Ca", new MetricsAllowableValueRangeStub("Ca", "Afferent Coupling",
                false, 0.00, 0.00, 0, 500));
        metrics.put("I", new MetricsAllowableValueRangeStub("I", "Instability",
                true, 0.00, 1.00, 0, 0));
        metrics.put("A", new MetricsAllowableValueRangeStub("A", "Abstractness",
                true, 0.00, 1.00, 0, 0));
        metrics.put("D", new MetricsAllowableValueRangeStub("D", "Normalized Distance From Main Sequence",
                true, 0.00, 0.70, 0, 0));

        //MOOD metrics set
        metrics.put("MHF", new MetricsAllowableValueRangeStub("MHF", "Method Hiding Factor",
                true, 0.095, 0.369, 0, 0));
        metrics.put("AHF", new MetricsAllowableValueRangeStub("AHF", "Attribute Hiding Factor",
                true, 0.677, 1.0, 0, 0));
        metrics.put("MIF", new MetricsAllowableValueRangeStub("MIF", "Method Inheritance Factor",
                true, 0.609, 0.844, 0, 0));
        metrics.put("AIF", new MetricsAllowableValueRangeStub("AIF", "Attribute Inheritance Factor",
                true, 0.374, 0.757, 0, 0));
        metrics.put("CF", new MetricsAllowableValueRangeStub("CF", "Coupling Factor",
                true, 0.00, 0.243, 0, 0));
        metrics.put("PF", new MetricsAllowableValueRangeStub("PF", "Polymorphism Factor",
                true, 0.017, 0.151, 0, 0));

        //Methods metrics set
//        metrics.put("CND", new MetricStub("CND", "Condition Nesting Depth",
//                false, 0.00, 0.00, 0, 50));
        metrics.put("LOC", new MetricsAllowableValueRangeStub("LOC", "Lines Of Code",
                false, 0.00, 0.00, 0, 500));
//        metrics.put("LND", new MetricStub("LND", "Loop Nesting Depth",
//                false, 0.00, 0.00, 0, 4));
//        metrics.put("CC", new MetricStub("CC", "McCabe Cyclomatic Complexity",
//                false, 0.00, 0.00, 0, 50));
//        metrics.put("NOL", new MetricStub("NOL", "Number Of Loops",
//                false, 0.00, 0.00, 0, 10));
//        metrics.put("FANIN", new MetricStub("FANIN", "Fan-In",
//                false, 0.00, 0.00, 0, 5));
//        metrics.put("FANOUT", new MetricStub("FANOUT", "Fan-Out",
//                false, 0.00, 0.00, 0, 5));
    }

    public Map<String, MetricsAllowableValueRangeStub> getMetrics() {
        return new HashMap<>(metrics);
    }

    public void setMetrics(Map<String, MetricsAllowableValueRangeStub> metrics) {
        this.metrics.clear();
        this.metrics.putAll(metrics);
    }

    public List<MetricsAllowableValueRangeStub> getMetricsList() {
        return new ArrayList<>(metrics.values());
    }

    public void updateMetrics(@NotNull HashMap<String, MetricsAllowableValueRangeStub> newMetrics) {
        newMetrics.forEach((key, value) -> metrics.replace(key, value));
    }

    @Override
    public synchronized MetricsAllowableValuesRanges getState() {
        return this;
    }

    @Override
    public synchronized void loadState(MetricsAllowableValuesRanges state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MetricsAllowableValueRangesSettings";
    }

    public static class MetricsAllowableValueRangeStub {
        private String name;
        private String description;
        private boolean doubleValue;
        private double minDoubleValue;
        private double maxDoubleValue;
        private long minLongValue;
        private long maxLongValue;

        public MetricsAllowableValueRangeStub(String name, String description, boolean doubleValue,
                                              double minDoubleValue, double maxDoubleValue, long minLongValue, long maxLongValue) {
            this.name = name;
            this.description = description;
            this.doubleValue = doubleValue;
            this.minDoubleValue = minDoubleValue;
            this.maxDoubleValue = maxDoubleValue;
            this.minLongValue = minLongValue;
            this.maxLongValue = maxLongValue;
        }

        public MetricsAllowableValueRangeStub(){}

        public boolean isDoubleValue() {
            return doubleValue;
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

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MetricsAllowableValueRangeStub)) return false;
            MetricsAllowableValueRangeStub that = (MetricsAllowableValueRangeStub) o;
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
            return Objects.hash(getName(), getDescription(), doubleValue, getMinDoubleValue(),
                    getMaxDoubleValue(), getMinLongValue(), getMaxLongValue());
        }
    }

}
