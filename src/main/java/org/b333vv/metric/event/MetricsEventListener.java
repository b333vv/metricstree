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

package org.b333vv.metric.event;

import com.intellij.util.messages.Topic;
import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.PackageElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;

public interface MetricsEventListener {

        Topic<MetricsEventListener> TOPIC = new Topic<>("MetricsEventListener", MetricsEventListener.class);

        default void classMetricsValuesEvolutionCalculated(@NotNull DefaultTreeModel defaultTreeModel) {
        }

        default void clearProjectMetricsTree() {
        }

        default void clearClassFitnessFunctionPanel() {
        }

        default void clearPackageFitnessFunctionPanel() {
        }

        default void clearClassMetricsValuesEvolutionTree() {
        }

        default void buildClassMetricsTree() {
        }

        default void buildProjectMetricsTree() {
        }

        default void showClassMetricsTree(boolean showClassMetricsTree) {
        }

        default void refreshClassMetricsTree() {
        }

        default void cancelMetricsValuesEvolutionCalculation() {
        }

        default void metricsProfileSelected(FitnessFunction profile) {
        }

        default void javaClassSelected(ClassElement javaClass) {
        }

        default void packageLevelJavaClassSelected(ClassElement javaClass) {
        }

        default void javaPackageSelected(PackageElement javaPackage) {
        }

        default void projectMetricsTreeIsReady(DefaultTreeModel treeModel,
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void pieChartIsReady(@org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void printInfo(String info) {
        }

        default void categoryChartIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void xyChartIsReady(@org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void classByMetricTreeIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void classLevelFitnessFunctionIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void packageLevelFitnessFunctionIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void packageLevelFitnessFunctionSelected(FitnessFunction fitnessFunction) {
        }

        default void profilesBoxChartIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void currentMetricType(MetricType metricType) {
        }

        default void profilesHeatMapChartIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void profilesRadarChartIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void profilesCategoryChartIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void currentMetricProfile(FitnessFunction fitnessFunction) {
        }

        default void metricTreeMapIsReady(
                        @org.jetbrains.annotations.Nullable com.intellij.openapi.module.Module module) {
        }

        default void setProjectPanelBottomText(String text) {
        }

        default void setProfilePanelBottomText(String text) {
        }

        default void projectTreeMapCellClicked(ClassElement javaClass) {
        }

        default void profileTreeMapCellClicked(ClassElement javaClass) {
        }

        default void profileTreeMapIsReady() {

        }

        default void clearProjectPanel() {
        }

        default void projectMetricsHistoryXyChartIsReady() {
        }

        default void plusButtonPressed() {
        }

        default void minusButtonPressed() {
        }
}
