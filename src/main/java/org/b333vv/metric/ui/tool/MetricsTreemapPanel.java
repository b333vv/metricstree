/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.ui.tool;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.ui.chart.builder.ProfileBoxChartBuilder;
import org.b333vv.metric.ui.info.BottomPanel;
import org.b333vv.metric.ui.info.PackageMetricsTable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsTreemapPanel extends SimpleToolWindowPanel {
    private static final String SPLIT_PROPORTION_PROPERTY = "SPLIT_PROPORTION";

    private JBPanel<?> rightPanel;
    private JPanel mainPanel;
    private JPanel chartPanel;
    private PackageMetricsTable packageMetricsTable;

    private final Project project;
    private Map<Integer, JBTabbedPane> rightPanelMap = new HashMap<>();
    private List<ProfileBoxChartBuilder.BoxChartStructure> boxChartList;


    public MetricsTreemapPanel(Project project) {
        super(false, true);
        this.project = project;
        createUIComponents();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Metrics Toolbar",
                (DefaultActionGroup) actionManager.getAction("Metrics.MetricsChartToolbar"), false);
        actionToolbar.setOrientation(SwingConstants.VERTICAL);
        setToolbar(actionToolbar.getComponent());
        MetricsEventListener metricsEventListener = new MetricsChartEventListener();
        project.getMessageBus().connect(project).subscribe(MetricsEventListener.TOPIC, metricsEventListener);
    }

    private void createUIComponents() {
        BottomPanel bottomPanel = new BottomPanel();
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(bottomPanel.getPanel(), BorderLayout.SOUTH);
        rightPanel = new JBPanel<>(new BorderLayout());
        super.setContent(createSplitter(mainPanel, rightPanel));
    }

    private JComponent createSplitter(JComponent c1, JComponent c2) {
        float savedProportion = PropertiesComponent.getInstance(project)
                .getFloat(MetricsTreemapPanel.SPLIT_PROPORTION_PROPERTY, (float) 0.65);

        final JBSplitter splitter = new JBSplitter(false);
        splitter.setFirstComponent(c1);
        splitter.setSecondComponent(c2);
        splitter.setProportion(savedProportion);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.addPropertyChangeListener(Splitter.PROP_PROPORTION,
                evt -> PropertiesComponent.getInstance(project).setValue(MetricsTreemapPanel.SPLIT_PROPORTION_PROPERTY,
                        Float.toString(splitter.getProportion())));
        return splitter;
    }

    private void clear() {
        rightPanel.removeAll();
        mainPanel.removeAll();
        updateUI();
        createUIComponents();
    }

    private class MetricsChartEventListener implements MetricsEventListener {

        @Override
        public void clearChartsPanel() {
            clear();
        }

    }
}
