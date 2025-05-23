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

package org.b333vv.metric.ui.treemap.presentation;

import com.intellij.openapi.project.Project;
import org.b333vv.metric.event.MetricsEventListener;
import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class MetricTreeMap<N> extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Consumer<String> selectionAction;
    private Consumer<JavaClass> clickedAction;

    protected TreeModel<N> model;
    protected TreeMapLayout<N> layout;
    protected TreeModel<Rectangle<N>> rectangles;
    protected Rectangle<N> selected;
    protected N currentRoot;
    protected BufferedImage image;
    protected BuildControl buildControl;
    protected RectangleRenderer<N, Graphics2D, Color> renderer = DefaultRectangleRenderer.defaultInstance();
    protected LabelProvider<N> labelProvider;
    protected ColorProvider<N, Color> colorProvider;
    protected List<SelectionChangeListener<N>> listeners;
    protected GraphicsConfiguration gc;

    public MetricTreeMap() {
        this(true);
    }

    public MetricTreeMap(final boolean supportNavigation) {
        super();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent componentevent) {
                recalculate();
            }
        });
        if (supportNavigation) {
            new TreeMapMouseController<N>(this);
        }
    }

    public void setRectangleRenderer(final RectangleRenderer<N, Graphics2D, Color> aRenderer) {
        renderer = aRenderer;
    }

    public void setSelectionChangedAction(Consumer<String> action) {
        selectionAction = action;
    }

    public Consumer<String> getSelectionChangedAction() {
        return selectionAction;
    }

    public void setClickedAction(Consumer<JavaClass> action) {
        clickedAction = action;
    }

    public Consumer<JavaClass> getClickedAction() {
        return clickedAction;
    }

    public void setLabelProvider(final LabelProvider<N> aProvider) {
        labelProvider = aProvider;
    }

    public void setColorProvider(final ColorProvider<N, Color> aProvider) {
        colorProvider = aProvider;
    }

    public void refresh() {
        image = rebuildImage(getWidth(), getHeight(), rectangles);
        repaint();
    }

    public void setTreeModel(final WeightedTreeModel<N> aModel) {
        if (layout == null) {
            layout = new SquarifiedLayout<N>(2);
        }
        model = aModel;
        currentRoot = aModel.getRoot();
        selected = null;
        rectangles = null;
        image = null;
        recalculate();
    }

    public void addSelectionChangeListener(final SelectionChangeListener<N> aListener) {
        if (listeners == null) {
            listeners = new ArrayList<>(2);
        }
        listeners.add(aListener);
    }


    public void setTreeMapLayout(final TreeMapLayout<N> aLayout) {
        layout = aLayout;
    }

    @Override
    public void paintComponent(final Graphics gr) {
        final Graphics2D g = (Graphics2D) gr;
        if (image != null) {
            final int w = getWidth();
            final int h = getHeight();
            g.drawImage(image, 0, 0, w, h, null);
            if (selected != null) {
                final int imgw = image.getWidth();
                final int imgh = image.getHeight();
                if (imgw != w || imgh != h) {
                    final AffineTransform transform = new AffineTransform();
                    transform.scale(w/(double) imgw, h/(double) imgh);
                    g.setTransform(transform);
                }
                renderer.highlight(g, rectangles, selected, colorProvider, labelProvider);
            }
        } else {
            drawBusy(g);
        }
    }

    protected void drawBusy(final Graphics2D gr) {
        gr.setColor(getBackground());
        gr.fillRect(0, 0, getWidth(), getHeight());
    }

    protected void render(final Graphics2D g, final TreeModel<Rectangle<N>> rects) {
        final Rectangle<N> root = rects.getRoot();
        if (root != null) {
            if (colorProvider == null) {
                colorProvider = new DefaultColorProvider<N>();
            }
            final Fifo<Rectangle<N>> queue = new Fifo<>();
            queue.push(rects.getRoot());
            while (queue.notEmpty()) {
                final Rectangle<N> node = queue.pull();
                render(g, rects, node);
                if (rects.hasChildren(node)) {
                    for (Iterator<Rectangle<N>> i = rects.getChildren(node); i.hasNext(); ) {
                        queue.push(i.next());
                    }
                }
            }
        }
    }

    protected void render(final Graphics2D g, final TreeModel<Rectangle<N>> rects, final Rectangle<N> rect) {
        renderer.render(g, rects, rect, colorProvider, labelProvider);
    }

    protected boolean selectRectangle(final int x, final int y) {
        if (selected == null || !selected.contains(x, y)) {
            selected = findRectangle(x, y);
            if (selected != null && listeners != null) {
                final String label;
                if (labelProvider != null) {
                    label = labelProvider.getLabel(rectangles, selected);
                } else {
                    label = null;
                }
                for (int i = listeners.size()-1; i >= 0; i--) {
                    listeners.get(i).selectionChanged(rectangles, selected, label);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected Rectangle<N> findRectangle(final int x, final int y) {
        Rectangle<N> result;
        if (rectangles != null) {
            result = rectangles.getRoot();
            if (result.contains(x, y)) {
                while (rectangles.hasChildren(result)) {
                    boolean found = false;
                    for (Iterator<Rectangle<N>> i = rectangles.getChildren(result); i.hasNext(); ) {
                        final Rectangle<N> candidate = i.next();
                        if (candidate.contains(x, y)) {
                            result = candidate;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        break;
                    }
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    protected synchronized void recalculate() {
        if (model != null) {
            if (buildControl != null) {
                buildControl.cancel();
                buildControl = null;
            }
            final BuildControl ctrl = new BuildControl();
            final SwingWorker<TreeModel<Rectangle<N>>, Object> worker = new Worker<N>(this, ctrl);
            if (!GraphicsEnvironment.isHeadless()) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            worker.execute();
            buildControl = ctrl;
        }
    }

    protected BufferedImage rebuildImage(final int width, final int height, final TreeModel<Rectangle<N>> rects) {
        if (width*height > 0) {
            final BufferedImage result;
            if (GraphicsEnvironment.isHeadless()) {
                result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            } else {
                if (gc == null) {
                    final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    final GraphicsDevice gs = ge.getDefaultScreenDevice();
                    gc = gs.getDefaultConfiguration();
                }
                result = gc.createCompatibleImage(width, height);
            }
            final Graphics2D g = result.createGraphics();
            try {
                render(g, rects);
            } finally {
                g.dispose();
            }
            return result;
        } else {
            return null;
        }
    }

    private class Worker<N> extends SwingWorker<TreeModel<Rectangle<N>>, Object> {

        private final BuildControl buildControl;
        private final MetricTreeMap<N> metricTreeMap;
        private final int width, height;
        private BufferedImage image;

        public Worker(final MetricTreeMap<N> aMap, final BuildControl aControl) {
            super();
            metricTreeMap = aMap;
            buildControl = aControl;
            width = aMap.getWidth();
            height = aMap.getHeight();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected TreeModel<Rectangle<N>> doInBackground(){
            final TreeModel<Rectangle<N>> result;
            if (metricTreeMap.layout instanceof GenericTreeMapLayout) {
                result = ((GenericTreeMapLayout<N, Number>) metricTreeMap.layout).layout((GenericWeightedTreeModel<N, Number>) metricTreeMap.model, metricTreeMap.currentRoot, width, height, buildControl);
            } else if (metricTreeMap.layout instanceof TreeMapLayout) {
                result = metricTreeMap.layout.layout((WeightedTreeModel<N>) metricTreeMap.model, metricTreeMap.currentRoot, width, height, buildControl);
            } else {
                throw new IllegalStateException("cannot handle model with layout "+ metricTreeMap.layout);
            }
            if (!buildControl.isCanceled()) {
                image = metricTreeMap.rebuildImage(width, height, result);
            }
            return result;
        }

        @Override
        protected void done() {
            try {
                final TreeModel<Rectangle<N>> newRects = get();
                if (!buildControl.isCanceled()) {
                    synchronized (metricTreeMap) {
                        metricTreeMap.rectangles = newRects;
                        final Rectangle<N> root = newRects.getRoot();
                        if (root != null) {
                            metricTreeMap.currentRoot = root.getNode();
                        }
                        if (!GraphicsEnvironment.isHeadless()) {
                            metricTreeMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        }
                        metricTreeMap.image = image;
                        metricTreeMap.selected = null;
                        metricTreeMap.buildControl = null;

                        if (!GraphicsEnvironment.isHeadless()) {
                            final Point point = metricTreeMap.getMousePosition();
                            if (point != null) {
                                metricTreeMap.selectRectangle(point.x, point.y);
                            }
                        }
                    }
                    metricTreeMap.repaint();
                }
            } catch (InterruptedException | ExecutionException e) {
//                MetricTreeMap.this.project.getMessageBus().syncPublisher(MetricsEventListener.TOPIC).printInfo(e.getMessage());
            }
        }
    }
}
