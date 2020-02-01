package org.jacoquev.ui.info;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import org.jacoquev.model.code.JavaClass;
import org.jacoquev.model.code.JavaMethod;
import org.jacoquev.model.metric.Metric;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

public class BottomPanel {
    private final JPanel panel;
    private JTextPane metricDescription;

    public BottomPanel() {

        panel = new JPanel(new BorderLayout());
        panel.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));
        clear();
        show();
    }

    public void clear() {
        nothingToDisplay(false);
    }

    public void setData(@NotNull Metric metric) {
        String description = metric.getDescription();
        if (description == null) {
            nothingToDisplay(true);
            return;
        }
        updateDescription(description);
    }

    public void setData(@NotNull JavaClass javaClass) {
        String description = javaClass.getName();
        if (description == null) {
            nothingToDisplay(true);
            return;
        }
        updateDescription(description);
    }

    public void setData(@NotNull JavaMethod javaMethod) {
        String description = javaMethod.getName();
        if (description == null) {
            nothingToDisplay(true);
            return;
        }
        updateDescription(description);
    }

    private void nothingToDisplay(boolean error) {
        metricDescription = null;
        panel.removeAll();

        String txt;
        if (error) {
            txt = "Couldn't find a description";
        } else {
            txt = "Select a node to see its description";
        }

        JComponent titleComp = new JLabel(txt, SwingConstants.LEFT);
        panel.add(titleComp, BorderLayout.CENTER);
        panel.revalidate();
    }

    private void updateDescription(String text) {

        panel.removeAll();
        metricDescription = new JTextPane();
        metricDescription.setText(text);
        panel.add(metricDescription, BorderLayout.CENTER);

        panel.revalidate();
    }

    public void show() {
        panel.setVisible(true);
    }

    public JComponent getPanel() {
        return panel;
    }

}
