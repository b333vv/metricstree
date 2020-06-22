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

package org.b333vv.metric.exec;

import com.intellij.openapi.project.Project;
import org.apache.commons.io.FilenameUtils;
import org.b333vv.metric.export.CsvExporter;
import org.b333vv.metric.export.Exporter;
import org.b333vv.metric.export.XmlExporter;
import org.b333vv.metric.ui.tool.ProjectMetricsPanel;
import org.b333vv.metric.util.MetricsUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class MetricExportProcessor {

    private final Project project;


    public MetricExportProcessor(Project project) {
        this.project = project;
    }

    public final void execute() {
        JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jFileChooser.setDialogTitle("Choose a file name:");
        jFileChooser.setSelectedFile(new File(project.getName()));
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(".csv (classes metrics)", "csv");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(".xml (all metrics)", "xml");
        jFileChooser.addChoosableFileFilter(csvFilter);
        jFileChooser.addChoosableFileFilter(xmlFilter);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        int returnValue = jFileChooser.showSaveDialog(null);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = jFileChooser.getSelectedFile();
        final FileFilter filter = jFileChooser.getFileFilter();
        Exporter exporter;
        if (filter.equals(csvFilter)) {
            if (!FilenameUtils.getExtension(selectedFile.getName()).equalsIgnoreCase("csv")) {
                selectedFile = new File(selectedFile.getParentFile(), FilenameUtils.getBaseName(selectedFile.getName()) + ".csv");
            }
            exporter = new CsvExporter();
        } else {
            if (!FilenameUtils.getExtension(selectedFile.getName()).equalsIgnoreCase("xml")) {
                selectedFile = new File(selectedFile.getParentFile(), FilenameUtils.getBaseName(selectedFile.getName()) + ".xml");
            }
            exporter = new XmlExporter();
        }
        String fileName = selectedFile.getAbsolutePath();
        exporter.export(fileName, project.getComponent(ProjectMetricsPanel.class).getJavaProject());
    }
}
