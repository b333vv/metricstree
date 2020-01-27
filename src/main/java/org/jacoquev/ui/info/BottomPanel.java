package org.jacoquev.ui.info;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import org.jacoquev.model.metric.Metric;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

public class BottomPanel {
  private final JPanel panel;
  private JTextPane metricDescription;

  public BottomPanel() {

    panel = new JPanel(new BorderLayout());
    panel.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));
    setMetric(null);
    show();
  }

  public void setMetric(@Nullable Metric metric) {
    if (metric == null) {
      nothingToDisplay(false);
    } else {
      String description = metric.getDescription();
      if (description == null) {
        nothingToDisplay(true);
        return;
      }
      updateDescription(description);
    }
  }


  private void nothingToDisplay(boolean error) {
    metricDescription = null;
    panel.removeAll();

    String txt;
    if (error) {
      txt = "Couldn't find an extended description for the metric";
    } else {
      txt = "Select a metric to see their extended description";
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
