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

import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.*;

import java.awt.*;

public class DefaultRectangleRenderer<N> implements RectangleRenderer<N, Graphics2D, Color> {

    private static final RectangleRenderer<Object, Graphics2D, Color> DEFAULT = new DefaultRectangleRenderer<>();
    @SuppressWarnings("unchecked")
    public static final <R> RectangleRenderer<R, Graphics2D, Color> defaultInstance() {
        return (RectangleRenderer<R, Graphics2D, Color>) DEFAULT;
    }

    @Override
    public void render(final Graphics2D graphics, final TreeModel<Rectangle<N>> model,
                       final Rectangle<N> rectangle, final ColorProvider<N, Color> colorProvider,
                       final LabelProvider<N> labelProvider) {
        graphics.setColor(colorProvider.getColor(model, rectangle));
        graphics.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    @Override
    public void highlight(final Graphics2D graphics, final TreeModel<Rectangle<N>> model,
                          final Rectangle<N> rectangle, final ColorProvider<N, Color> colorProvider,
                          final LabelProvider<N> labelProvider) {
        graphics.setColor(Color.WHITE);
        graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

}
