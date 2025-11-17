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

package org.b333vv.metric.ui.treemap.builder;

import org.b333vv.metric.model.code.*;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.ui.treemap.model.*;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.presentation.CushionRectangleRendererEx;
import org.b333vv.metric.ui.treemap.presentation.MetricTreeMap;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TreeMapBuilder implements SelectionChangeListener<CodeElement>, LabelProvider<CodeElement> {

    private final GenericTreeModel<CodeElement> model = new GenericTreeModel<>();
    private final MetricTreeMap<CodeElement> treeMap = new MetricTreeMap<>();

    public TreeMapBuilder(@NotNull ProjectElement projectElement) {
        treeMap.setLabelProvider(this);
        treeMap.setRectangleRenderer(new CushionRectangleRendererEx<>(160));
        treeMap.addSelectionChangeListener(this);
        treeMap.setTreeMapLayout(new SquarifiedLayout<>(64));

//        treeMap.setColorProvider(new MetricTypeColorProvider(MetricType.NCSS));
//        treeMap.setColorProvider(colorProvider);

        treeMap.setTreeModel(TreeMapModel.createTreeModel(projectElement));
    }

//    public void setColorProvider(@NotNull ColorProvider<JavaCode, Color> colorProvider) {
//        treeMap.setColorProvider(colorProvider);
//    }

    public MetricTreeMap<CodeElement> getTreeMap() {
        return treeMap;
    }

    @Override
    public void selectionChanged(TreeModel<Rectangle<CodeElement>> model, Rectangle<CodeElement> rectangle, String label) {
        if (label != null) {
            final CodeElement node = rectangle.getNode();
            if (node instanceof ClassElement) {
                ClassElement clazz = (ClassElement) node;
                String name = null;
                if (clazz.getPsiClass() != null) {
                    name = clazz.getPsiClass().getQualifiedName();
                } else if (clazz.getKtClassOrObject() != null) {
                    // Try FQ name first, fallback to simple name
                    if (clazz.getKtClassOrObject().getFqName() != null) {
                        name = clazz.getKtClassOrObject().getFqName().asString();
                    } else {
                        name = clazz.getKtClassOrObject().getName();
                    }
                }
                if (name == null || name.isEmpty()) {
                    name = clazz.getName();
                }
                Consumer<String> selectionAction = treeMap.getSelectionChangedAction();
                if (selectionAction != null) {
                    selectionAction.accept("Class: " + name);
                }
            }
        }
    }

    @Override
    public String getLabel(final TreeModel<Rectangle<CodeElement>> model, final Rectangle<CodeElement> rectangle) {
        if (rectangle.getNode() instanceof PackageElement) {
            return rectangle.getNode().getName();
        }
        return "";
    }
}
