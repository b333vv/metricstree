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

package org.b333vv.metric.ui.treemap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SquarifiedLayout<N> implements TreeMapLayout<N>, Cancelable, Serializable {

	private static final long serialVersionUID = 1L;

	protected final int maxDepth;

	public SquarifiedLayout(final int nestingDepth) {
		maxDepth = nestingDepth;
	}

	@Override
	public TreeModel<Rectangle<N>> layout(final WeightedTreeModel<N> model, final N startNode,
										  final int width, final int height, final Cancelable cancelable) {
		final RectangleImpl<N> root = new RectangleImpl<>(startNode, 0, 0, width, height);
		final RectangleModelImpl<N> result = new RectangleModelImpl<>(root);
		squarify(result, root, new ComparatorImpl<>(model), 0, cancelable);
		if (cancelable.isCanceled()) {
			return RectangleModelImpl.emptyModel();
		} else {
			return result;
		}
	}

	protected void squarify(final RectangleModelImpl<N> result, final RectangleImpl<N> rectangle,
							final ComparatorImpl<N> comparator, final int depth, final Cancelable cancelable) {
		if (depth < maxDepth && !cancelable.isCanceled()) {
			final N n = rectangle.getNode();
			final WeightedTreeModel<N> model = comparator.getModel();
			if (model.hasChildren(n)) {
				long total = 0;
				final Iterator<N> i = model.getChildren(n);
				final List<N> nodes = new ArrayList<>(i instanceof IteratorSize<?> ?((IteratorSize<?>) i).size():16);
				while (i.hasNext()) {
					final N c = i.next();
					nodes.add(c);
					total += model.getWeight(c);
				}
				final int max = nodes.size();
				if (max > 2) {
					nodes.sort(comparator);
					squarify(result, rectangle, rectangle, comparator, nodes, 0, max, total, depth, cancelable);
				} else {
					if (max == 2) {
						final N one = nodes.get(0);
						final N two = nodes.get(1);
						if (comparator.compare(one, two) > 0) {
							nodes.set(0, two);
							nodes.set(1, one);
						}
					}
					slice(result, rectangle, rectangle, comparator, nodes, 0, max, total, depth, cancelable);
				}
			}
		}
	}

	protected void squarify(final RectangleModelImpl<N> result, final RectangleImpl<N> parent,
							final RectangleImpl<N> rectangle, final ComparatorImpl<N> comparator,
							final List<N> nodes, final int start, final int end, final long weight, final int depth,
							final Cancelable cancelable) {
		if (end-start > 2) {
			final WeightedTreeModel<N> model = comparator.getModel();
			float aspectRatio = Float.MAX_VALUE, last;
			int i = start;
			long sum = 0;
			final int[] rect = new int[2];
			do {
				final N n = nodes.get(i++);
				final long nodeWeight = model.getWeight(n);
				sum += nodeWeight;
				rect[0] = rectangle.w;
				rect[1] = rectangle.h;
				fit(rect, sum, weight);
				fit(rect, nodeWeight, sum);
				last = aspectRatio;
				aspectRatio = aspectRatio(rect[0],rect[1]);
				if (aspectRatio > last) {
					sum -= model.getWeight(nodes.get(--i));
					final double frac = sum/(double) weight;
					if (frac > 0 && frac < 1) {
						final RectangleImpl<N>[] r = rectangle.split(frac);
						squarify(result, parent, r[0], comparator, nodes, start, i, sum, depth, cancelable);
						squarify(result, parent, r[1], comparator, nodes, i, end, weight-sum, depth, cancelable);
						return;
					} else {
						// need to slice
						break;
					}
				}
			} while (i<end);
		}
		slice(result, parent, rectangle, comparator, nodes, start, end, weight, depth, cancelable);
	}

	protected void slice(final RectangleModelImpl<N> result, final RectangleImpl<N> parent, final RectangleImpl<N> r, final ComparatorImpl<N> comparator, final List<N> nodes, final int start, final int max, final long w, final int depth, final Cancelable cancelable) {
		if (cancelable.isCanceled()) {
			return;
		}
		final WeightedTreeModel<N> model = comparator.getModel();
		final double dw = (double) w;
		final int last = max-1;
		if (r.w < r.h) {
			final int sx = r.x;
			int sy = r.y;
			final int maxy = r.y+r.h;
			for (int i = start; i < max && sy < maxy; i++) {
				final N c = nodes.get(i);
				final long wc = model.getWeight(c);
				final int step = (i!=last)?(int) Math.round((r.h * wc) / dw) : r.h - (sy - r.y);
				if (step > 0) {
					final RectangleImpl<N> child = createRectangle(c, sx, sy, r.w, step);
					if (child != null) {
						result.addChild(parent, child);
						if (model.hasChildren(c)) {
							squarify(result, child, comparator, depth + 1, cancelable);
						}
						sy += step;
					}
				} else {
					final int rest = r.h - (sy - r.y);
					if (rest > 0) {
						final RectangleImpl<N> child = createRectangle(c, sx, sy, r.w, 1);
						if (child != null) {
							result.addChild(parent, child);
							sy++;
						}
					}
				}
			}
		} else {
			int sx = r.x;
			final int sy = r.y;
			final int maxx = r.x + r.w;
			for (int i = start; i < max && sx < maxx; i++) {
				final N c = nodes.get(i);
				final long wc = model.getWeight(c);
				final int step = (i!=last)?(int) Math.round((r.w * wc) / dw) : r.w - (sx - r.x);
				if (step > 0) {
					final RectangleImpl<N> child = createRectangle(c, sx, sy, step, r.h);
					if (child != null) {
						result.addChild(parent, child);
						if (model.hasChildren(c)) {
							squarify(result, child, comparator, depth+1, cancelable);
						}
						sx += step;
					}
				} else {
					final int rest = r.w - (sx - r.x);
					if (rest > 0) {
						final RectangleImpl<N> child = createRectangle(c, sx, sy, 1, r.h);
						if (child != null) {
							result.addChild(parent, child);
							sx++;
						}
					}
				}
			}
		}
	}

	protected RectangleImpl<N> createRectangle(final N n, final int x, final int y, final int w, final int h) {
		return new RectangleImpl<N>(n, x, y, w, h);
	}

	private void fit(final int[] rect, final long weight, final long total) {
		final int s = Math.min(rect[0], rect[1]);
		final int l = Math.max(rect[0], rect[1]);
		rect[0] = (int) (weight * l / (double) total);
		rect[1] = s;
		if (rect[0] == 0) {
			rect[0] = 1;
		}
	}

	private float aspectRatio(final int a, final int b) {
		if (a > b) {
			return a / (float) b;
		} else {
			return b / (float) a;
		}
	}

	private static class ComparatorImpl<N> implements Comparator<N> {

		private final WeightedTreeModel<N> model;

		public ComparatorImpl(final WeightedTreeModel<N> aModel) {
			model = aModel;
		}

		protected WeightedTreeModel<N> getModel() {
			return model;
		}

		@Override
		public int compare(final N o1, final N o2) {
			return (int) (model.getWeight(o2) - model.getWeight(o1));
		}

	}

	@Override
	public final boolean isCanceled() {
		return false;
	}
}
