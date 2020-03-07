package org.b333vv.metric.ui.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@State(name = "ClassMetricsTreeSettings", storages = {@Storage("class-metrics-tree.xml")})
public final class ClassMetricsTreeSettings implements PersistentStateComponent<ClassMetricsTreeSettings>, ProjectComponent {

    private static final String CLASS_LEVEL = "Class level";
    private static final String METHOD_LEVEL = "Method level";
    private static final String CHIDAMBER_KEMERER_METRICS_SET = "Chidamber-Kemerer metrics set";
    private static final String LORENZ_KIDD_METRICS_SET = "Lorenz-Kidd metrics set";
    private static final String LI_HENRY_METRICS_SET = "Li-Henry metrics set";

    private final List<MetricsTreeSettingsStub> classTreeMetrics = new ArrayList<>();

    private boolean showClassMetricsTree;

    public ClassMetricsTreeSettings() {
        loadInitialValues();
    }

    private void loadInitialValues() {
        //Chidamber-Kemerer metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("WMC", "Weighted Methods Per Class",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("DIT", "Depth Of Inheritance Tree",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOC", "Number Of Children",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("RFC", "Response For A Class",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LCOM", "Lack Of Cohesion In Methods",
                CLASS_LEVEL, CHIDAMBER_KEMERER_METRICS_SET, true));

        //Lorenz-Kidd metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOA", "Number Of Attributes",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOO", "Number Of Operations",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOAM", "Number Of Added Methods",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOOM", "Number of Overridden Methods",
                CLASS_LEVEL, LORENZ_KIDD_METRICS_SET, true));

        //Li-Henry metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("SIZE2", "Number Of Attributes And Methods",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOM", "Number Of Methods",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("MPC", "Message Passing Coupling",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("DAC", "Data Abstraction Coupling",
                CLASS_LEVEL, LI_HENRY_METRICS_SET, true));

        //Methods metrics set
        classTreeMetrics.add(new MetricsTreeSettingsStub("CND", "Condition Nesting Depth",
                METHOD_LEVEL, "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LOC", "Lines Of Code",
                METHOD_LEVEL, "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("LND", "Loop Nesting Depth",
                METHOD_LEVEL, "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("CC", "McCabe Cyclomatic Complexity",
                METHOD_LEVEL, "", true));
        classTreeMetrics.add(new MetricsTreeSettingsStub("NOL", "Number Of Loops",
                METHOD_LEVEL, "", true));
//        classTreeMetrics.add(new MetricsTreeSettingsStub("FIN", "Fan-In",
//                METHOD_LEVEL, "", true));
//        classTreeMetrics.add(new MetricsTreeSettingsStub("FOUT", "Fan-Out",
//                METHOD_LEVEL, "", true));

        showClassMetricsTree = true;
    }

    public void setClassTreeMetrics(List<MetricsTreeSettingsStub> metrics) {
        this.classTreeMetrics.clear();
        this.classTreeMetrics.addAll(metrics);
    }

    @Override
    public synchronized ClassMetricsTreeSettings getState() {
        return this;
    }

    @Override
    public synchronized void loadState(@NotNull ClassMetricsTreeSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ClassMetricsTreeSettings";
    }

    public List<MetricsTreeSettingsStub> getMetricsList() {
        Comparator<MetricsTreeSettingsStub> compareByLevelAndName = Comparator
                .comparing(MetricsTreeSettingsStub::getLevel)
                .thenComparing(MetricsTreeSettingsStub::getName);
        return classTreeMetrics.stream()
                .sorted(compareByLevelAndName)
                .collect(Collectors.toList());
    }

    public List<MetricsTreeSettingsStub> getClassTreeMetrics() {
        return classTreeMetrics;
    }

    public void setShowClassMetricsTree(boolean showClassMetricsTree) {
        this.showClassMetricsTree = showClassMetricsTree;
    }

    public boolean isShowClassMetricsTree() {
        return showClassMetricsTree;
    }
}
