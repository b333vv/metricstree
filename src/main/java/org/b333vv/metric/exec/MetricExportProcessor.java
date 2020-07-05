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

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.b333vv.metric.export.CsvExporter;
import org.b333vv.metric.export.Exporter;
import org.b333vv.metric.export.XmlExporter;
import org.b333vv.metric.ui.tool.ProjectMetricsPanel;

public class MetricExportProcessor {

    private final Project project;


    public MetricExportProcessor(Project project) {
        this.project = project;
    }

    public void exportToCsv() {
        String fileName = getFileName("csv");
        if (fileName != null) {
            Exporter exporter = new CsvExporter();
            exporter.export(fileName, project.getComponent(ProjectMetricsPanel.class).getJavaProject());
        }
    }

    public void exportToXml() {
        String fileName = getFileName("xml");
        if (fileName != null) {
            Exporter exporter = new XmlExporter();
            exporter.export(fileName, project.getComponent(ProjectMetricsPanel.class).getJavaProject());
        }
    }

    private String getFileName(String extension) {
        FileSaverDescriptor fileSaverDescriptor = new FileSaverDescriptor("Choose A File Name:",
                "Choose a file name to export metrics data", extension);
        FileSaverDialog fileSaverDialog = FileChooserFactory.getInstance().createSaveFileDialog(fileSaverDescriptor, project);
        VirtualFile outputDir = VfsUtil.getUserHomeDir();
        String fileName = project.getName() + (SystemInfo.isMac ? "." + extension : "");
        VirtualFileWrapper fileWrapper = fileSaverDialog.save(outputDir, fileName);
        if (fileWrapper != null) {
            return fileWrapper.getFile().getAbsolutePath();
        }
        return null;
    }
}
