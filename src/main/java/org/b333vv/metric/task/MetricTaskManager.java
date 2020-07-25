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

package org.b333vv.metric.task;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaFile;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.profile.MetricProfile;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class MetricTaskManager {

    public static void sureDependenciesAreInCache(@NotNull ProgressIndicator indicator) {
        if (MetricTaskCache.instance().getUserData(MetricTaskCache.DEPENDENCIES) == null) {
            DependenciesTask dependenciesTask = new DependenciesTask();
            dependenciesTask.run(indicator);

            ReadAction.run(() -> {
                MetricTaskCache.instance().getJavaFiles().flatMap(JavaFile::classes)
                        .forEach(javaClass -> {
                            javaClass.removeMetric(MetricType.CBO);
                        });
                MetricTaskCache.instance().getJavaFiles().flatMap(JavaFile::classes)
                        .forEach(javaClass -> {
                            javaClass.accept(MetricType.CBO.visitor());
                        });
            });
        }
    }

    public static JavaProject getClassAndMethodModel(@NotNull ProgressIndicator indicator) {
        JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.CLASS_AND_METHODS_METRICS);
        if (javaProject == null) {
            ClassAndMethodMetricTask classAndMethodMetricTask = new ClassAndMethodMetricTask();
            classAndMethodMetricTask.run(indicator);
            javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.CLASS_AND_METHODS_METRICS);
        }
        return javaProject;
    }

    public static JavaProject getPackageModel(@NotNull ProgressIndicator indicator) {
        JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PACKAGE_METRICS);
        if (javaProject == null) {
            PackageMetricTask packageMetricTask = new PackageMetricTask();
            packageMetricTask.run(indicator);
            javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PACKAGE_METRICS);
        }
        return javaProject;
    }

    public static JavaProject getPackageOnlyModel(@NotNull ProgressIndicator indicator) {
        JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PACKAGE_ONLY_METRICS);
        if (javaProject == null) {
            PackageOnlyMetricTask packageMetricTask = new PackageOnlyMetricTask();
            packageMetricTask.run(indicator);
            javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PACKAGE_ONLY_METRICS);
        }
        return javaProject;
    }

    public static JavaProject getProjectModel(@NotNull ProgressIndicator indicator) {
        JavaProject javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PROJECT_METRICS);
        if (javaProject == null) {
            ProjectMetricTask projectMetricTask = new ProjectMetricTask();
            projectMetricTask.run(indicator);
            javaProject = MetricTaskCache.instance().getUserData(MetricTaskCache.PROJECT_METRICS);
        }
        return javaProject;
    }

    public static Map<MetricProfile, Set<JavaClass>> getMetricProfilesDistribution(@NotNull ProgressIndicator indicator) {
        Map<MetricProfile, Set<JavaClass>> classesByMetricProfile = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_PROFILES);
        if (classesByMetricProfile == null) {
            MetricProfilesTask metricProfilesTask = new MetricProfilesTask();
            metricProfilesTask.run(indicator);
            classesByMetricProfile = MetricTaskCache.instance().getUserData(MetricTaskCache.METRIC_PROFILES);
        }
        return classesByMetricProfile;
    }

    public static String getFileName(String extension, Project project) {
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
