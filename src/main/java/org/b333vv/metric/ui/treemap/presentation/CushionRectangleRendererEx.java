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

import org.b333vv.metric.ui.treemap.model.ColorProvider;
import org.b333vv.metric.ui.treemap.model.LabelProvider;
import org.b333vv.metric.ui.treemap.model.Rectangle;
import org.b333vv.metric.ui.treemap.model.TreeModel;

import java.awt.*;

public class CushionRectangleRendererEx<N> extends CushionRectangleRenderer<N> {

	public CushionRectangleRendererEx(final int colorRangeSize) {
		super(colorRangeSize);
	}

	protected void highlightParents(final Graphics2D graphics, final TreeModel<Rectangle<N>> model, final Rectangle<N> rectangle, final ColorProvider<N, Color> colorProvider, final LabelProvider<N> labelProvider) {
		graphics.setColor(Color.RED);
		graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth()-1, rectangle.getHeight()-1);

		final Rectangle<N> root = model.getRoot();
		Rectangle<N> runner = rectangle, last;
		do {
			last = runner;
			runner = model.getParent(runner);
		} while (runner != root && runner != null);
		if (last != root) {
			graphics.setColor(Color.YELLOW);
			graphics.drawRect(last.getX(), last.getY(), last.getWidth()-1, last.getHeight()-1);
		}
	}

}
