package org.b333vv.metric.ui.treemap.builder;

import org.b333vv.metric.model.code.JavaClass;
import org.b333vv.metric.model.code.JavaCode;
import org.b333vv.metric.model.code.JavaProject;
import org.b333vv.metric.model.metric.MetricType;
import org.b333vv.metric.model.metric.value.Value;
import org.b333vv.metric.ui.treemap.model.GenericTreeModel;
import org.b333vv.metric.ui.treemap.model.WeightedTreeModel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TreeMapModel extends GenericTreeModel<JavaCode> {

	public static WeightedTreeModel<JavaCode> createTreeModel(final JavaProject javaProject) {
		final TreeMapModel result = new TreeMapModel();
		result.add(javaProject, 0, null);
		List<JavaClass> sortedClasses = javaProject.allClasses()
				.sorted(Comparator.comparing(JavaCode::getName))
				.collect(Collectors.toList());
		for (JavaClass javaClass : sortedClasses) {
			result.add(javaClass, getWeight(javaClass), javaProject);
		}
		return result;
	}

	private static long getWeight(JavaClass javaClass) {
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
