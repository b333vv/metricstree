package org.b333vv.metric.ui.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@State(name = "MetricsAllowableValueRanges", storages = {@Storage("metrics-allowed-value-ranges.xml")})
public final class MetricsAllowableValuesRangesSettings implements PersistentStateComponent<MetricsAllowableValuesRangesSettings>, ProjectComponent {

    private Map<String, MetricsAllowableValuesRangeStub> controlledMetrics = new HashMap<>();
    private Map<String, MetricsAllowableValuesRangeStub> unControlledMetrics = new HashMap<>();
    private Map<String, MetricsAllowableValuesRangeStub> temporaryControlledMetrics = new HashMap<>();
    private Map<String, MetricsAllowableValuesRangeStub> temporaryUnControlledMetrics = new HashMap<>();

    public MetricsAllowableValuesRangesSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        controlledMetrics.put("WMC", new MetricsAllowableValuesRangeStub("WMC", "Weighted Methods Per Class", "Class",
                false, 0.00, 0.00, 0, 24));
        controlledMetrics.put("DIT", new MetricsAllowableValuesRangeStub("DIT", "Depth Of Inheritance Tree", "Class",
                false, 0.00, 0.00, 0, 5));
        controlledMetrics.put("CBO", new MetricsAllowableValuesRangeStub("CBO", "Coupling Between Object", "Class",
                false, 0.00, 0.00, 0, 13));
        controlledMetrics.put("RFC", new MetricsAllowableValuesRangeStub("RFC", "Response For A Class", "Class",
                false, 0.00, 0.00, 0, 44));


        //Lorenz-Kidd metrics set
        controlledMetrics.put("NOA", new MetricsAllowableValuesRangeStub("NOA", "Number Of Attributes", "Class",
                false, 0.00, 0.00, 0, 40));
        controlledMetrics.put("NOO", new MetricsAllowableValuesRangeStub("NOO", "Number Of Operations", "Class",
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put("NOOM", new MetricsAllowableValuesRangeStub("NOOM", "Number of Overridden Methods", "Class",
                false, 0.00, 0.00, 0, 3));

        //Robert C. Martin metrics set
        controlledMetrics.put("Ce", new MetricsAllowableValuesRangeStub("Ce", "Efferent Coupling", "Package",
                false, 0.00, 0.00, 0, 20));
        controlledMetrics.put("Ca", new MetricsAllowableValuesRangeStub("Ca", "Afferent Coupling", "Package",
                false, 0.00, 0.00, 0, 500));
        controlledMetrics.put("I", new MetricsAllowableValuesRangeStub("I", "Instability", "Package",
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put("A", new MetricsAllowableValuesRangeStub("A", "Abstractness", "Package",
                true, 0.00, 1.00, 0, 0));
        controlledMetrics.put("D", new MetricsAllowableValuesRangeStub("D", "Normalized Distance From Main Sequence", "Package",
                true, 0.00, 0.70, 0, 0));

        //MOOD metrics set
        controlledMetrics.put("MHF", new MetricsAllowableValuesRangeStub("MHF", "Method Hiding Factor", "Project",
                true, 0.095, 0.369, 0, 0));
        controlledMetrics.put("AHF", new MetricsAllowableValuesRangeStub("AHF", "Attribute Hiding Factor", "Project",
                true, 0.677, 1.0, 0, 0));
        controlledMetrics.put("MIF", new MetricsAllowableValuesRangeStub("MIF", "Method Inheritance Factor", "Project",
                true, 0.609, 0.844, 0, 0));
        controlledMetrics.put("AIF", new MetricsAllowableValuesRangeStub("AIF", "Attribute Inheritance Factor", "Project",
                true, 0.374, 0.757, 0, 0));
        controlledMetrics.put("CF", new MetricsAllowableValuesRangeStub("CF", "Coupling Factor", "Project",
                true, 0.00, 0.243, 0, 0));
        controlledMetrics.put("PF", new MetricsAllowableValuesRangeStub("PF", "Polymorphism Factor", "Project",
                true, 0.017, 0.151, 0, 0));

        //Methods metrics set
        controlledMetrics.put("LOC", new MetricsAllowableValuesRangeStub("LOC", "Lines Of Code", "Method",
                false, 0.00, 0.00, 0, 500));


        //Chidamber-Kemerer metrics set
        unControlledMetrics.put("NOC", new MetricsAllowableValuesRangeStub("NOC", "Number Of Children", "Class",
                false, 0.00, 0.00, 0, 100));
        unControlledMetrics.put("LCOM", new MetricsAllowableValuesRangeStub("LCOM", "Lack Of Cohesion In Methods", "Class",
                false, 0.00, 0.00, 0, 500));

        //Lorenz-Kidd metrics set
        unControlledMetrics.put("NOAM", new MetricsAllowableValuesRangeStub("NOAM", "Number Of Added Methods", "Class",
                false, 0.00, 0.00, 0, 10));

        //Li-Henry metrics set
        unControlledMetrics.put("SIZE2", new MetricsAllowableValuesRangeStub("SIZE2", "Number Of Attributes And Methods", "Class",
                false, 0.00, 0.00, 0, 130));
        unControlledMetrics.put("NOM", new MetricsAllowableValuesRangeStub("NOM", "Number Of Methods", "Class",
                false, 0.00, 0.00, 0, 25));
        unControlledMetrics.put("MPC", new MetricsAllowableValuesRangeStub("MPC", "Message Passing Coupling", "Class",
                false, 0.00, 0.00, 0, 10));
        unControlledMetrics.put("DAC", new MetricsAllowableValuesRangeStub("DAC", "Data Abstraction Coupling", "Class",
                false, 0.00, 0.00, 0, 15));

        //Methods metrics set
        unControlledMetrics.put("CND", new MetricsAllowableValuesRangeStub("CND", "Condition Nesting Depth", "Method",
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put("LND", new MetricsAllowableValuesRangeStub("LND", "Loop Nesting Depth", "Method",
                false, 0.00, 0.00, 0, 4));
        unControlledMetrics.put("CC", new MetricsAllowableValuesRangeStub("CC", "McCabe Cyclomatic Complexity", "Method",
                false, 0.00, 0.00, 0, 50));
        unControlledMetrics.put("NOL", new MetricsAllowableValuesRangeStub("NOL", "Number Of Loops", "Method",
                false, 0.00, 0.00, 0, 10));
        unControlledMetrics.put("FANIN", new MetricsAllowableValuesRangeStub("FANIN", "Fan-In", "Method",
                false, 0.00, 0.00, 0, 5));
        unControlledMetrics.put("FANOUT", new MetricsAllowableValuesRangeStub("FANOUT", "Fan-Out", "Method",
                false, 0.00, 0.00, 0, 5));
    }

    public Map<String, MetricsAllowableValuesRangeStub> getControlledMetrics() {
        return new HashMap<>(controlledMetrics);
    }

    public void setControlledMetrics(Map<String, MetricsAllowableValuesRangeStub> metrics) {
        controlledMetrics.clear();
        controlledMetrics.putAll(metrics);
    }

    public Map<String, MetricsAllowableValuesRangeStub> getUnControlledMetrics() {
        return new HashMap<>(unControlledMetrics);
    }

    public void setUnControlledMetrics(Map<String, MetricsAllowableValuesRangeStub> metrics) {
        unControlledMetrics.clear();
        unControlledMetrics.putAll(metrics);
    }

    public List<MetricsAllowableValuesRangeStub> getControlledMetricsList() {
        Comparator<MetricsAllowableValuesRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsAllowableValuesRangeStub::getLevel)
                .thenComparing(MetricsAllowableValuesRangeStub::getName);
        return controlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<MetricsAllowableValuesRangeStub> getUnControlledMetricsList() {
        Comparator<MetricsAllowableValuesRangeStub> compareByLevelAndName = Comparator
                .comparing(MetricsAllowableValuesRangeStub::getLevel)
                .thenComparing(MetricsAllowableValuesRangeStub::getName);
        return unControlledMetrics.values().stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized MetricsAllowableValuesRangesSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull MetricsAllowableValuesRangesSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MetricsAllowableValueRangesSettings";
    }

    public void removeFromUnControlledMetrics(String key) {
        temporaryControlledMetrics.put(key, unControlledMetrics.get(key));
        unControlledMetrics.remove(key);
    }

    public void addToUnControlledMetrics(MetricsAllowableValuesRangeStub metricsAllowableValuesRangeStub) {
        unControlledMetrics.put(metricsAllowableValuesRangeStub.getName(), metricsAllowableValuesRangeStub);
        temporaryUnControlledMetrics.put(metricsAllowableValuesRangeStub.getName(), metricsAllowableValuesRangeStub);
    }

    public void returnAllToUnControlledMetrics() {
        unControlledMetrics.putAll(temporaryControlledMetrics);
    }

    public void returnAllToControlledMetrics() {
        unControlledMetrics.entrySet().removeIf(e -> temporaryUnControlledMetrics.containsKey(e.getKey()));
    }

    public void clearTemporaryControlledMetrics() {
        temporaryControlledMetrics.clear();
    }

    public void clearTemporaryUnControlledMetrics() {
        temporaryUnControlledMetrics.clear();
    }
}
