package org.b333vv.metric.ui.treemap.builder;

import org.b333vv.metric.model.code.ClassElement;
import org.b333vv.metric.model.code.CodeElement;
import org.b333vv.metric.model.code.ProjectElement;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.treemap.model.GenericTreeModel;
import org.b333vv.metric.ui.treemap.model.WeightedTreeModel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TreeMapModel extends GenericTreeModel<CodeElement> {

	public static WeightedTreeModel<CodeElement> createTreeModel(final ProjectElement javaProject) {
		final TreeMapModel result = new TreeMapModel();
		result.add(javaProject, 0, null);
		List<ClassElement> sortedClasses = javaProject.allClasses()
				.sorted(Comparator.comparing(CodeElement::getName))
				.collect(Collectors.toList());
		for (ClassElement javaClass : sortedClasses) {
			result.add(javaClass, getWeight(javaClass), javaProject);
		}
		return result;
	}

	private static long getWeight(ClassElement javaClass) {
		if (javaClass.metric(MetricType.NCSS) == null) {
			return 0;
		}
		if (javaClass.metric(MetricType.NCSS).getPsiValue() == Value.UNDEFINED) {
			return 0;
		} else {
			return javaClass.metric(MetricType.NCSS).getPsiValue().longValue();
		}
	}
}
